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

(ns redmapel.node
  (:require [clojure.pprint :as pp]
            [degel.cljutil.utils :as utils]
            [degel.cljutil.devutils :as dev]))

(defn make-node
  "Create a new root node. In principle, this should also be used for every
   internal node in the tree too, but that does not play well with my use of
   `assoc-in` and friends.

  It would be nice to use `defrecord` here but, again, that does not play with
  `assoc-in`. (The problem is that `assoc-in` automatically creates new
  internal nodes as simple maps).

  Each node contains the following keys:

  * `:value` The value stored at this node
  * `:path` Path to this node. The vector of keys used to access this
            value. This backpointer is here primarily for debugging and
            diagnostic. It may disappear someday.
  * `:children` Children of this node. Maps next path elements to nodes.
  * `:watchers` Nested map of functions to be alerted on a change to this node
            or any of its descendants."
  []
  {})


(defn describe-node
  "Print the contents of a node tree in human-readable form."
  ([node]
     (describe-node node []))
  ([node path]
     (pp/cl-format true "~S~@[ (but claims ~S)~] ~8,8T- ~S ~8,8T~S~%"
                   path
                   (and (not= path (:path node)) (:path node))
                   (:value node)
                   (map (comp type second) (:watchers node)))
     (doseq [[key child] (seq (:children node))]
       (describe-node child (conj path key)))))


(defn- node-path
  "Private implementation-dependent sequence (e.g. for assoc-in) to an internal
  node."
  [path]
  (vec (interleave (repeat :children) path)))

(defn- value-path
  "Implementation-dependent sequence to the value held in an internal node."
  [path]
  (-> path node-path (conj :value)))

(defn- path-path [path]
  "Implementation-dependent sequence to the path held in an internal node."
  (-> path node-path (conj :path)))

(defn- watchers-path [path]
  "Implementation-dependent sequence to the watchers list held in an internal
   node."
  (-> path node-path (conj :watchers)))


(defn fetch
  "Fetch a value stored in the tree, keyed by path.

   ex: `(fetch my-root [:users :account-info :user-id])`"
  [node path]
  (get-in node (value-path path)))


(defn- heads
  "Utility function, should move into a different project.

   Return the partial heads of a sequence.

   ex: `(heads (range 3))`

  -> `([] [0] [0 1] [0 1 2])`"
  [l]
  (reductions conj [] l))


(defn- partition-pairs
  "Utility function, should move somewhere. Also needs a better name. Compare
   with `utils/group-results`.

   Organize a sequence of `[key value]` items, grouping by key.

   ex: `(partition-pairs [[:o 1] [:e 2] [:o 3] [:e 4] [:o 5] [:e 6]])`

   -> ` {:e (2 4 6), :o (1 3 5)}`"
  [l]
  (into {}
        (map #(vector (ffirst %) (map second %))
             (partition-by first (sort-by first l)))))


(defn watchers
  "Return a map of all the watchers observing a node, organized by watch type.
   Note that this includes watchers on ancestor nodes."
  [node path]
  (partition-pairs
   (mapcat #(get-in node (watchers-path %))
           (heads path))))


(defn put
  "Add a value to the tree, keyed by path.

   ex. `(put my-root [:users :account-info :user-id] \"David\")`"
  [node path value]
  (let [old-value (fetch node path)]
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

(defn update
  "Modify a value in the tree, keyed by path.

   ex: `(update my-root [:users :account-info :login-attempts] inc)`"
  [node path f & args]
  (let [old-value (fetch node path)]
    (put node path (apply f old-value args))))


(defn watch
  "Register a watch function that will be called whenever a value is changed,
   at or below the specified path.

   Watch types:

   * :before - Called before the change occurs. Can return false to abort the
           operation.
   * :after - Called after the change occurs. Used for side-effects."
  [node path id watch-type watch-fn]
  (-> node
      (update-in (watchers-path path) conj [watch-type watch-fn])
      (update ['all-watches id] conj [watch-type watch-fn])))
