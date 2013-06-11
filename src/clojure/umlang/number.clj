;; Copyright (c) 2013 Dylon Edwards <dylon.edwards@gmail.com>
;;
;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:
;;
;; The above copyright notice and this permission notice shall be included in
;; all copies or substantial portions of the Software.
;;
;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
;; SOFTWARE.

(ns umlang.number
  (:require [umlang.metric.reporter :as reporter])
  (:import [org.apache.commons.lang3.builder HashCodeBuilder ToStringBuilder]))

(defprotocol INumber
  "Defines necessary methods for all number sets"
  (value-of
    [this])
  (**
    [this exponent]
    "Raises this number to some power")
  (*
    [this multiplicand]
    "Multiplies this number with some other")
  (/
    [this dividend]
    "Divides this number by some other")
  (+
    [this addend]
    "Adds some number to this one")
  (-
    [this subtrahend]
    "Subtracts some number from this one")
  (=
    [this other])
  (<
    [this other])
  (<=
    [this other])
  (>
    [this other])
  (>=
    [this other])
  (inc
    [this])
  (dec
    [this])
  (even?
    [this])
  (odd?
    [this]))

(defmacro def->integer [Type ctor constrained?]
  `(do
    (declare ~ctor)
    (deftype ~Type [^BigInteger value#]
      INumber

      (value-of
        [this#]
        value#)
      (**
        [this# exponent#]
        (~ctor (.pow value# (.value-of exponent#))))
      (*
        [this# multiplicand#]
        (~ctor (.multiply value# (value-of multiplicand#))))
      (/
        [this# dividend#]
        (~ctor (.divide value# (value-of dividend#))))
      (+
        [this# addend#]
        (~ctor (.add value# (value-of addend#))))
      (-
        [this# subtrahend#]
        (~ctor (.subtract value# (value-of subtrahend#))))
      (=
        [this# other#]
        (clojure.core/= 0 (.compareTo value# (value-of other#))))
      (<
        [this# other#]
        (clojure.core/< (.compareTo value# (value-of other#)) 0))
      (<=
        [this# other#]
        (clojure.core/<= (.compareTo value# (value-of other#)) 0))
      (>
        [this# other#]
        (clojure.core/> (.compareTo value# (value-of other#)) 0))
      (>=
        [this# other#]
        (clojure.core/>= (.compareTo value# (value-of other#)) 0))
      (inc
        [this#]
        (~ctor (.add value# BigInteger/ONE)))
      (dec
        [this#]
        (~ctor (.subtract value# BigInteger/ONE)))
      (even?
        [this#]
        (-> (.mod value# (BigInteger/valueOf 2)) (.equals BigInteger/ZERO)))
      (odd?
        [this#]
        (not (even? this#)))

      Object

      (equals
        [this# other#]
        (and (instance? ~Type other#)
          (not (nil? other#))
          (= this# other#)))
      (hashCode
        [this#]
        (-> (HashCodeBuilder.)
          (.append value#)
          (.toHashCode)))
      (toString
        [this#]
        (-> (ToStringBuilder. this#)
          (.append "value" value#)
          (.toString))))

    (defmulti ~ctor class)
    (defmethod ~ctor BigInteger [value#]
      {:pre [(not (nil? value#)) (~constrained? value#)]}
      (~(symbol (str Type ".")) value#))
    (defmethod ~ctor Number [number#]
      {:pre [(not (nil? number#))]}
      (~ctor (BigInteger/valueOf (.longValue number#))))
    (defmethod ~ctor INumber [number#]
      (~ctor (value-of number#)))
    (defmethod ~ctor ~Type [number#]
      (~ctor (value-of number#)))))

(def->integer NaturalNumber natural #(clojure.core/not= (.signum %) -1))
(def->integer IntegerNumber integer (fn [value] true))
(def->integer IntegerNumber+ integer+ #(clojure.core/= (.signum %) 1))
(def->integer IntegerNumber- integer- #(clojure.core/= (.signum %) -1))

