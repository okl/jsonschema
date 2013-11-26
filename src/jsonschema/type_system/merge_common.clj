(ns jsonschema.type-system.merge-common
  "Things that are needed by both merge.clj and simplify.clj."
  (:require [jsonschema.type-system.types :refer :all]
            [clojure.set :refer [union]]
            [roxxi.utils.print :refer [print-expr]]))

;; # Core abstraction of merging types

(defn- type-type [type]
  "Returns `:scalar` if type is any kind of scalar. When we add custom types,
  we can derive the set of scalar types dynamically."
  (let [type-sigil (getType type)]
    (or (#{:document} type-sigil)
        (#{:collection} type-sigil)
        (#{:union} type-sigil)
        (and (#{:int :real :str :null :bool :date} type-sigil)
             :scalar))))

(defprotocol TypeMerger
  "Merges two types"
  (type-merge [_ type1 type2]))

(deftype TypeMergerImpl [type*type=>merge-fn]
  TypeMerger
  (type-merge [_ t1 t2]
    (let [merge-fn (get-in type*type=>merge-fn [(type-type t1) (type-type t2)])]
      (merge-fn t1 t2))))

(defn make-type-merger [type*type=>merge-fn]
  (TypeMergerImpl. type*type=>merge-fn))

;; # Predicates

(defn congruent? [d1 d2]
  (= (:properties d1) (:properties d2)))

(defn incongruent? [d1 d2]
  (not (congruent? d1 d2)))

;; There are two outcomes when you try to merge types:
;;   1) The merge result has the same type as one or both input types
;;      (possibly with different metadata)
;;   2) The merge result has a different type than both input types
;;      (and it will necessarily be a Union type).
;; Two things are `compatible` if merging them looks like case 1.
;; Otherwise, they are not compatible, and merging them will result in a
;; Union type.
;;
;; More generally, the question "Are a and b compatible?" can be interpreted
;; as "Do a and b look alike?"
(defn- type-dispatcher [t1 t2 merge-notion]
  (cond
   (or (union-type? t1) (union-type? t2))
   :union
   (and (scalar-type? t1) (scalar-type? t2))
   :scalar
   (and (document-type? t1) (document-type? t2))
   :document
   (and (collection-type? t1) (collection-type? t2))
   :collection
   (and (satisfies? Typeable t1) (satisfies? Typeable t2))
   :non-mergeable-types))

(defmulti compatible? type-dispatcher)

(defmethod compatible? :scalar [t1 t2 merge-notion]
  (same-type? t1 t2))

(defmethod compatible? :document [t1 t2 merge-notion]
  (condp = merge-notion
    :merge (congruent? t1 t2)
    :simplify true))

(defmethod compatible? :collection [t1 t2 merge-notion]
  (condp = merge-notion
    :merge (cond
            (and (empty-collection? t1) (empty-collection? t2)) true
            (or (empty-collection? t1) (empty-collection? t2)) false
            :else (compatible? (:coll-of t1) (:coll-of t2) merge-notion))
    :simplify (cond
               (or (empty-collection? t1) (empty-collection? t2)) true
               :else (compatible? (:coll-of t1) (:coll-of t2) merge-notion))))

(defmethod compatible? :union [t1 t2 merge-notion]
  (condp = merge-notion
    :merge
    (and (union-type? t1)
         (union-type? t2)
         (every? true? (map
                        (fn [union-element]
                          (some #(compatible? union-element % merge-notion)
                                (:union-of t2)))
                        (:union-of t1)))
         (every? true? (map
                        (fn [union-element]
                          (some #(compatible? union-element % merge-notion)
                                (:union-of t1)))
                        (:union-of t2))))
    :simplify
    true))

(defmethod compatible? :non-mergeable-types [t1 t2 merge-notion]
  false)

(defmethod compatible? :default [t1 t2 merge-notion]
  (throw (RuntimeException.
          (str "Don't know how to decide if these two objects "
               "are compatible: " t1 ", " t2))))

(defn merge-compatible? [t1 t2]
  (compatible? t1 t2 :merge))

(defn simplify-compatible? [t1 t2]
  (compatible? t1 t2 :simplify))


;; Assumes the notion of compatibility is transitive... so if you add
;; another merge-notion, make sure your new notion of compatibility
;; is transitive.
(defn reduce-compatible-types [types compatible? merge-two-compatible-things]
  "If input is [a1 a2 b1 c1 a3 c2], then output
is [merged(a1 a2 a3) b1 merged(c1 c2)]"
  (reduce (fn [merged-types type]
            ;; if it's mergeable with something ...
            (if (some #(compatible? type %) merged-types)
              ;; ... do the merge
              (map (fn [merged-type]
                     (if (compatible? merged-type type)
                       (merge-two-compatible-things merged-type type)
                       merged-type))
                   merged-types)
              ;; ... otherwise insert it
              (conj merged-types type)))
          []
          types))


;; # Unions and Collections helpers

(defn- one? [a-seq]
  (= (count a-seq) 1))

(defn- flatten-nested-unions [types]
  "Only flattens by one level."
  (let [non-unions (remove union-type? types)
        unions (filter union-type? types)
        flattened-unions (reduce
                          (fn [vector union] (concat vector (:union-of union)))
                          []
                          unions)]
    (union non-unions flattened-unions)))


(defn turn-into-a-union [type-reducer types]
  (if (some #(union-type? %) types)
    ;; This will recurse until all nested unions are unflattened.
    (turn-into-a-union type-reducer (flatten-nested-unions types))
    (let [unique-types (type-reducer types)]
      (cond
       (empty? types) nil
       (one? unique-types) (first unique-types)
       :else (make-union unique-types)))))

(defn turn-into-a-union-with [type-reducer & types]
  (turn-into-a-union type-reducer types))

(defn turn-into-a-collection [type-reducer types]
  (let [unique-types (type-reducer types)]
    (make-collection
     (cond
      (empty? unique-types) :nothing
      (one? unique-types) (first unique-types)
      :else (make-union unique-types)))))

(defn turn-into-a-collection-with [type-reducer & types]
  (turn-into-a-collection type-reducer types))
