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


(ns redmapel
  "# Main redmapel namespace

  Includes functions to create, access, and modify a redmapel state tree.
  For more info, see the readme file."
  (:require [redmapel.node :as rml-node]
            [degel.cljutil.devutils :as dev]))


(defmacro defmapel
  "### Initialize a state tree"
  ([rml-tree]
     `(def ~rml-tree (atom (rml-node/make-node))))
    ([doc rml-tree]
     `(def ~rml-tree ~doc (atom (rml-node/make-node)))))


(defn empty!
  "### Empty a state tree, discarding everything

   Note that this global action will not trigger any alerts."
   [rml-tree]
  (reset! rml-tree (rml-node/make-node)))


(defn describe
  "### Describe the tree
   Print a verbose description of the state tree's contents."
  [rml-tree]
  (rml-node/describe-node @rml-tree))


(defn fetch
  "### Fetch a value from the state tree
   Path is a vector of identifiers.

   ex: `(fetch tree [:users :account-info :user-id])`"
  [rml-tree path]
  (rml-node/fetch @rml-tree path))


(defn put!
  "### Store a value into the state tree
   Path is a vector of identifiers.

   ex: `(put! tree [:users :account-info :user-id] \"David\")`"
  [rml-tree path value]
  (swap! rml-tree rml-node/put path value))


(defn update!
  "### Modify a value in the state tree
   Path is a vector of identifiers.

  ex: `(put! tree [:users :account-info :user-id] \"David\")`"
  [rml-tree path f & args]
  (apply swap! rml-tree rml-node/update path f args))


(defn guard!
  "### Set up a guard function

  The function `f` is called when anyone tries to modify the tree at path,
  before the modification actually occurs . Note, that this watches the entire
  subtree below path. That is, if the path argument is `[:a :b]`, `f` will be
  called as a result of `(put! tree [:a :b]) or (put! tree [:a :b :c :d])`.

  The arguments passed to the function are `tree`, `path`, `old-value`, and
  `new-value`. Note that the path is of the actual value being changed, which
  might be far below the path passed to `guard!`.

  If `f` returns falsey, it will prevent the modification. If multiple guards are
  in place, any one returning falsey is sufficient to prevent the
  modification."
  [rml-tree path id f]
  (swap! rml-tree rml-node/watch path id :before f))


(defn alert!
  "### Set up a trigger function

  The function `f` is called when anyone modifies the tree at path, immediately
  after the modification actually occurs . Note, that this watches the entire
  subtree below path. That is, if the path argument is `[:a :b]`, `f` will be
  called as a result of `(put! tree [:a :b]) or (put! tree [:a :b :c :d])`.

  The arguments passed to the function are `tree`, `path`, `old-value`, and
  `new-value`. Note that the path is of the actual value being changed, which
  might be far below the path passed to `alert!`.

  The return value from `f` is ignored."
  [rml-tree path id f]
  (swap! rml-tree rml-node/watch path id :after f))
