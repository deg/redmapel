(ns redmapel.node
  (:require [clojure.pprint :as pp]
            [degel.cljutil.devutils :as dev]))

(defn make-node []
  {})


(defn dump-node
  ([node]
     (dump-node node []))
  ([node path]
     (pp/cl-format true "At ~S~@[ (but claims ~S)~] ~S~%"
                   path
                   (and (not= path (:path node)) (:path node))
                   (:value node))
     (doseq [[key child] (seq (:children node))]
       (dump-node child (conj path key)))))


(defn- node-path [path]
  (vec (interleave (repeat :children) path)))

(defn- value-path [path]
  (-> path node-path (conj :value)))

(defn- path-path [path]
  (-> path node-path (conj :path)))


(defn node-assoc [node path value]
  (assoc-in
   (assoc-in node
             (path-path path) path)
   (value-path path) value))


(defn node-get [node path]
  (get-in node (value-path path)))
