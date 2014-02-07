(ns jsonschema.db-types-test
  (:use clojure.test
        roxxi.utils.print
        jsonschema.transform)
  (:require [jsonschema.type-system.types :as types]
            [jsonschema.type-system.db-types.mysql :as mysql-db-types]))

;; (deftest mysql-col-def-str->col-parts-test []
;;   (= (mysql-db-types/mysql-col-def-str->col-map "int(10) unsigned") {:json-type :int :mysql-type-kw :int_unsigned :col-type "int"}))

;; INTEGER TYPES
(deftest mysql-tinyint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "tinyint(1)")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) -128))
    (is (= (types/getMax int-type) 127))))

(deftest mysql-utinyint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "tinyint(1) unsigned")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) 0))
    (is (= (types/getMax int-type) 255))))

(deftest mysql-smallint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "smallint(2)")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) -32768))
    (is (= (types/getMax int-type) 32767))))

(deftest mysql-usmallint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "smallint(2) unsigned")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) 0))
    (is (= (types/getMax int-type) 65535))))

(deftest mysql-mediumint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "mediumint(3)")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) -8388608))
    (is (= (types/getMax int-type) 8388607))))

(deftest mysql-umediumint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "mediumint(3) unsigned")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) 0))
    (is (= (types/getMax int-type) 16777215))))

(deftest mysql-int->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "int(10)")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) -2147483648))
    (is (= (types/getMax int-type) 2147483647))))

(deftest mysql-uint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "int(11) unsigned")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) 0))
    (is (= (types/getMax int-type) 4294967295))))

(deftest mysql-bigint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "bigint(20)")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) -9223372036854775808))
    (is (= (types/getMax int-type) 9223372036854775807))))

(deftest mysql-ubigint->json-type-test []
  (let [int-type (mysql-db-types/col-type->json-type "bigint(21) unsigned")]
    (is (= (type int-type) jsonschema.type_system.types.Int))
    (is (= (types/getMin int-type) 0))
    (is (= (types/getMax int-type) 18446744073709551615))))

;; DECIMAL & NUMERIC
(deftest mysql-dec30->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "decimal(3,0)")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

(deftest mysql-dec45->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "decimal")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

(deftest mysql-num24->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "numeric(2,4)")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

(deftest mysql-num24->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "numeric")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

;; FLOAT & DOUBLE
(deftest mysql-float->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "float")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

(deftest mysql-ufloat->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "float unsigned")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

(deftest mysql-double->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "double")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

(deftest mysql-udouble->json-type-test []
  (let [real-type (mysql-db-types/col-type->json-type "double unsigned")]
    (is (= (type real-type) jsonschema.type_system.types.Real))
    (is (= (types/getMin real-type) mysql-db-types/DECIMAL_MIN))
    (is (= (types/getMax real-type) mysql-db-types/DECIMAL_MAX))))

;; CHAR & VARCHAR
(deftest mysql-char->json-type-test []
  (let [str-type (mysql-db-types/col-type->json-type "char(10)")]
    (is (= (type str-type) jsonschema.type_system.types.Str))
    (is (= (types/getMin str-type) 0))
    (is (= (types/getMax str-type) 10))))

(deftest mysql-varchar->json-type-test []
  (let [str-type (mysql-db-types/col-type->json-type "varchar(255)")]
    (is (= (type str-type) jsonschema.type_system.types.Str))
    (is (= (types/getMin str-type) 0))
    (is (= (types/getMax str-type) 255))))

(deftest mysql-blob->json-type-test []
  (let [str-type (mysql-db-types/col-type->json-type "blob")]
    (is (= (type str-type) jsonschema.type_system.types.Str))
    (is (= (types/getMin str-type) 0))
    (is (= (types/getMax str-type) 65535))))

(deftest mysql-text->json-type-test []
  (let [str-type (mysql-db-types/col-type->json-type "text")]
    (is (= (type str-type) jsonschema.type_system.types.Str))
    (is (= (types/getMin str-type) 0))
    (is (= (types/getMax str-type) 65535))))

;; DATE TYPES