;;; Copyright (c) 2013, David Goldfarb. All rights reserved.
;;; Contact info: deg@degel.com
;;;
;;; The use and distribution terms for this software are covered by the Eclipse
;;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;; be found in the file epl-v10.html at the root of this distribution.
;;; By using this software in any fashion, you are agreeing to be bound by the
;;; terms of this license.
;;;
;;; You must not remove this notice, or any other, from this software.

(ns degel.redmapel.node
  (:require [degel.cljutil.utils :as utils]))

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


(defn describe
  "Print the contents of a node tree in human-readable form."
  ([node]
     (describe node []))
  ([node path]

     ;; No pprint yet in clojurescript
     (when (or (:value node) (:watchers node))
       ;; [TODO] cl-format tabbing was nice. Get it to work here too.
       (println path "   " (:value node)
                (map (comp type second) (:watchers node))))
     #_
     (pp/cl-format true "~S~@[ (but claims ~S)~] ~8,8T- ~S ~8,8T~S~%"
                   path
                   (and (not= path (:path node)) (:path node))
                   (:value node)
                   (map (comp type second) (:watchers node)))

     (doseq [[key child] (seq (:children node))]
       (describe child (conj path key)))))


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


(defn watchers
  "Return a map of all the watchers observing a node, organized by watch type.
   Note that this includes watchers on ancestor nodes."
  [node path]
  (utils/group-values-by-keys (mapcat #(get-in node (watchers-path %))
                                      (utils/heads path))
                              first second))


(defn put
  "Add a value to the tree, keyed by path.

   ex. `(put my-root [:users :account-info :user-id] \"David\")`

  The persister and unpersister arguments are for the sake of our
  atomic wrapper in ../redmapel.clj. The problem they are solving is
  that an after-method might also put something into the database.
  The atom must be updated with the correct state tree after each
  operation.
  [TODO] In the future, this needs to be re-examined and perhaps be
  replaced by either a queuing model (each put only happens after
  the previous one completes and updates the atom) or an transactional
  model (nested operations all succeed together and then all become
  visible in the atom)."
  [node path value & {:keys [persister unpersister]}]
  (let [old-value (fetch node path)]
    (if (= old-value value)
      node
      (let [{:keys [before after]} (watchers node path)]
        (if (every? #(% node path old-value value) before)
          (let [new-node
                ;; (This ugly nested assoc-in is needed because we are storing
                ;; both the value and the path into the tree)
                (assoc-in
                 (assoc-in node (path-path path) path)
                 (value-path path) value)]
            (if persister
              (do
                (persister new-node)
                (doseq [f after]
                  (f (unpersister) path old-value value))
                (unpersister))
              (do
                (doseq [f after]
                  (f new-node path old-value value))
                new-node)))
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

   id is an caller-supplied tag, that can later be passed to the unwatch
   function to remove this watch. If multiple watches are created with the
   same id, a single call to unwatch will remove all of them.

   Watch types:

   * :before - Called before the change occurs. Can return false to abort the
           operation.
   * :after - Called after the change occurs. Used for side-effects."
  [node path id watch-type watch-fn]
  (-> node
      (update-in (watchers-path path) conj [watch-type watch-fn id])
      (update ['all-watches id] conj path)))


(defn unwatch
  "Remove all the watch functions that were tagged with a particular id."
  [node id]
  ;; The logic here is a little bit tight, at least for my current level of
  ;; comfort with clojure. So a comment is in order:
  ;; We are using reduce to walk down the list of hints and remove the watch
  ;; functions from each of the referenced locations.
  ;; We also need to remove the hints. We could do this at the end, but the
  ;; code is more compact if we do it first, like this.
  (reduce (fn [node path]
            (update-in node (watchers-path path)
                       (fn [old] (remove #(= (utils/third %) id) old))))
          (put node ['all-watches id] nil)
          (fetch node ['all-watches id])))
