;;; Copyright (c) 2013 David Goldfarb. All rights reserved.
;;; Contact info: deg@degel.com
;;;
;;; The use and distribution terms for this software are covered by the Eclipse
;;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;; be found in the file epl-v10.html at the root of this distribution.
;;; By using this software in any fashion, you are agreeing to be bound by the
;;; terms of this license.
;;;
;;; You must not remove this notice, or any other, from this software.


(defproject degel/redmapel "0.1.12"
  :description "Redmapel manages trees of state."
  :url "https://github.com/deg/redmapel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [degel/degel-clojure-utils "0.1.20"]]

  :profiles {:dev
             {:plugins [[lein-marginalia "0.7.1"]]}})
