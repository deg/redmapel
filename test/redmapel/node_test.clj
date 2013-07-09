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
      (is (= nil (node-get node-root-11 [:a]))))))


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
