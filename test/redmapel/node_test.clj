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

(ns redmapel.node-test
  (:require [clojure.test :refer :all]
            [redmapel.node :refer :all]))

(deftest nodes
  (testing "Empty node"
    (is (empty? (make-node))))
  (let [empty-node (make-node)
        node-a-b-42 (put empty-node [:a :b] 42)
        node-a-c-d-17 (put empty-node [:a :c :d] 17)
        node-mix (put node-a-b-42 [:a :c :d] 17)
        node-root-11 (put empty-node [] 11)]
    (testing "node assoc and get"
      (is (= 42  (fetch node-a-b-42 [:a :b])))
      (is (= nil (fetch node-a-b-42 [:a])))
      (is (= nil (fetch node-a-b-42 [:a :c])))
      (is (= 17  (fetch node-a-c-d-17 [:a :c :d])))
      (is (= 42  (fetch node-mix [:a :b])))
      (is (= 17  (fetch node-mix [:a :c :d])))
      (is (= nil (fetch node-mix [])))
      (is (= 11  (fetch node-root-11 [])))
      (is (= nil (fetch node-root-11 [:a]))))
    (let [changed-node (put node-mix [:a :c :d] "Changed!")]
      (is (= 17         (fetch node-mix [:a :c :d])))
      (is (= "Changed!" (fetch changed-node [:a :c :d]))))))


(deftest node-internals
  (let [node-path #'redmapel.node/node-path
        value-path #'redmapel.node/value-path
        path-path #'redmapel.node/path-path]
    (testing "Paths"
      (is (= (node-path [:a :b :c])
             [:children :a :children :b :children :c]))
      (is (= (value-path [:a :b :c])
             [:children :a :children :b :children :c :value]))
      (is (= (path-path [:a :b :c])
             [:children :a :children :b :children :c :path]))
      (testing "Edge cases"
        (is (= (node-path []) []))
        (is (= (value-path []) [:value]))))))


(def test-history
  "Persistent store for testing watchers."
  (atom []))

(defn clear-test-history
  "Clear the test history. Typically called at start of test."
  []
  (reset! test-history []))

(defn record-history
  "Toy watch function. Just adds some info to the test history."
  [node path old new]
  (swap! test-history conj [path old new]))

(defn only-numbers
  "Toy filtering watch function. Allow only numeric data."
  [node path old new]
  (number? new))

(deftest test-watchers
  (let [state-tree (-> (make-node)
                       (put [:a] "A val")
                       (put [:b] "B val")
                       (put [:a :b] "A B val")
                       (put [:a :c] "A C val")
                       (put [:b :a] "B A val")
                       (watch [:a] :whatever :after record-history)
                       (watch [:a :b] :whatever :before only-numbers))]
    (testing "watch nodes"
      (clear-test-history)
      (-> state-tree
          (put [:a] "one")       ;; Should enter history.
          (put [:b] "two")       ;; Not under :a, should not enter history.
          (put [:a :b] "three")  ;; Not a number, should not enter history.
          (put [:a :b] 4))       ;; Should enter history.
      (is (= @test-history
             [[[:a]    "A val"   "one"]
              [[:a :b] "A B val" 4]])))))
