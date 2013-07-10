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
        node-a-b-42 (node-assoc empty-node [:a :b] 42)
        node-a-c-d-17 (node-assoc empty-node [:a :c :d] 17)
        node-mix (node-assoc node-a-b-42 [:a :c :d] 17)
        node-root-11 (node-assoc empty-node [] 11)]
    (testing "node assoc and get"
      (is (= 42  (node-get node-a-b-42 [:a :b])))
      (is (= nil (node-get node-a-b-42 [:a])))
      (is (= nil (node-get node-a-b-42 [:a :c])))
      (is (= 17  (node-get node-a-c-d-17 [:a :c :d])))
      (is (= 42  (node-get node-mix [:a :b])))
      (is (= 17  (node-get node-mix [:a :c :d])))
      (is (= nil (node-get node-mix [])))
      (is (= 11  (node-get node-root-11 [])))
      (is (= nil (node-get node-root-11 [:a]))))
    (let [changed-node (node-assoc node-mix [:a :c :d] "Changed!")]
      (is (= 17         (node-get node-mix [:a :c :d])))
      (is (= "Changed!" (node-get changed-node [:a :c :d]))))))


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


(deftest utility-internals
  ;; These will probably move to a different namespace someday.
  (let [heads #'redmapel.node/heads
        partition-pairs #'redmapel.node/partition-pairs]
    (testing "internal utilities"
      (is (= (heads (range 3)) '([] [0] [0 1] [0 1 2])))
      (is (= (partition-pairs [[:o 1] [:e 2] [:o 3] [:e 4] [:o 5] [:e 6]])
             '{:e (2 4 6), :o (1 3 5)})))))


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

(deftest node-watchers
  (let [state-tree (-> (make-node)
                       (node-assoc [:a] "A val")
                       (node-assoc [:b] "B val")
                       (node-assoc [:a :b] "A B val")
                       (node-assoc [:a :c] "A C val")
                       (node-assoc [:b :a] "B A val")
                       (node-watch [:a] :after record-history)
                       (node-watch [:a :b] :before only-numbers))]
    (testing "watch nodes"
      (clear-test-history)
      (-> state-tree
          (node-assoc [:a] "one")       ;; Should enter history.
          (node-assoc [:b] "two")       ;; Not under :a, should not enter history.
          (node-assoc [:a :b] "three")  ;; Not a number, should not enter history.
          (node-assoc [:a :b] 4))       ;; Should enter history.
      (is (= @test-history
             [[[:a]    "A val"   "one"]
              [[:a :b] "A B val" 4]])))))
