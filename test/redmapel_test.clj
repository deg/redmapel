;;; Copyright (c) David Goldfarb. All rights reserved.
;;; Contact info: deg@degel.com
;;;
;;; The use and distribution terms for this software are covered by the Eclipse
;;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;; be found in the file epl-v10.html at the root of this distribution.
;;; By using this software in any fashion, you are agreeing to be bound by the
;;; terms of this license.
;;;
;;; You must not remove this notice, or any other, from this software.

(ns redmapel-test
  (:require [clojure.test :refer :all]
            [redmapel :refer :all]))


(deftest put-fetch
  (defmapel tree1)
  (is (= (type tree1) clojure.lang.Atom))
  (put! tree1 [:a] 1)
  (put! tree1 [:a :b] 12)
  (put! tree1 [:b] 2)
  (put! tree1 [:b :a] 21)
  (is (= (fetch tree1 [:a]) 1))
  (is (= (fetch tree1 [:a :b]) 12))
  (is (= (fetch tree1 [:b]) 2))
  (is (= (fetch tree1 [:b :a]) 21))
  (is (= (fetch tree1 [:c]) nil)))

(deftest guard
  (defmapel tree1)
  (guard! tree1 [:a] (fn [_ _ _ new-value] (even? new-value)))
  (put! tree1 [:a :a] 1)
  (put! tree1 [:a :b] 2)
  (is (= (fetch tree1 [:a]) nil))
  (is (= (fetch tree1 [:a :a]) nil))
  (is (= (fetch tree1 [:a :b]) 2))
  (is (= (fetch tree1 [:a :c]) nil))))
