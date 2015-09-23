(ns linkletter.utils.link-preview
  (:require [linkletter.db.core :as db]
            [ring.util.http-response :refer [ok]]
            [ring.util.codec :as codec]
            [net.cgrand.enlive-html :as html]
            [cheshire.core :refer  [generate-string]]))


(defn fetch-url [] ; [url]

  ;(slurp url)
  ;(spit (slurp "http://aeon.co/magazine/science/the-universal-constants-that-drive-physicists-mad/")
  (slurp "test.html"))

;(html/html-resource (java.net.URL. "http://www.google.com"))
;(fetch-url)


