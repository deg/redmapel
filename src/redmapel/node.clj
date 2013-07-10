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

(defn- watchers-path [path]
  (-> path node-path (conj :watchers)))


(defn node-get [node path]
  (get-in node (value-path path)))

(defn- heads [l]
  (reductions conj [] l))

(defn- partition-pairs [l]
  (into {}
        (map #(vector (ffirst %) (map second %))
             (partition-by first l))))

(defn watchers [node path]
  (partition-pairs
   (mapcat #(get-in node (watchers-path %))
           (heads path))))


(defn node-assoc [node path value]
  (let [old-value (node-get node path)]
    (if (= old-value value)
      node
      (let [{:keys [before after]} (watchers node path)]
        (if (every? #(% node path old-value value) before)
          (let [new-node (assoc-in
                          (assoc-in node (path-path path) path)
                          (value-path path) value)]
            (doseq [f after]
              (f new-node path old-value value))
            new-node)
          node)))))


(defn toy-watch [node path old new]
  (dev/dbg "Watch" (str "Changed " path " from " old " to " new)))

(defn toy-pred [node path old new]
  (even? new))


(defn node-watch
  "Watch types:
   :before - can return false to abort the operation
   :after - just for side-effects"
  [node path watch-type watch-fn]
  (update-in node (watchers-path path) conj [watch-type watch-fn]))
