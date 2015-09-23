(ns linkletter.home-handler
  (:require [linkletter.db.core :as db]
            [ring.util.http-response :refer [ok]]
            [ring.util.codec :as codec]
            [cheshire.core :refer  [generate-string]]))


(defn encode-url [url]
  (codec/url-encode url))

(defn decode-url [url]
  (codec/url-decode url))

; user id is hard-coded for now
(defn get-user-id []
  1)

(defn clean-form-data [form-data]
  {:url (get form-data :link)
   :user_id (get-user-id)
   :title (get form-data :link-name)})

(def sample-form-data
  {:url "https%3A%2F%2Fgithub.com%2Fring-clojure%2Fring-codec"
   :user_id 1
   :title "This is supposed to be the url title"
   })

;(clean-form-data sample-form-data)

(defn get-form-data [req]
  (get req :params))

; check if link is the same link is already present in the database
(defn is-url-already-present [url]
  (prn "insise is-url-already-present")
  (< 0  (get (first
               (db/get-url-count {:url url}))
             :count)))

(defn insert-link! [req]
  (if (not (is-url-already-present (:link (:params req))))
    (db/insert-link<! (clean-form-data (get-form-data req)))
    {:error "URL already present"}))

;(defn insert-link! [request] 
 ; (encode-url (get-in request [:params :link])))


; get all links from database
(defn get-links []
  (db/get-links))


