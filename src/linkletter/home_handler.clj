(ns linkletter.home-handler
  (:require [linkletter.db.core :as db]
            [ring.util.http-response :refer [ok]]
            [ring.util.codec :as codec]
            [taoensso.timbre :as log]
            [linkletter.utils.link-preview :as preview]
            [cheshire.core :refer  [generate-string]]))



(defn encode-url [url]
  (codec/url-encode url))

(defn decode-url [url]
  (codec/url-decode url))

; user id is hard-coded for now
(defn get-user-id []
  
  1)

(defn escape-string [str]
  (clojure.string/escape str {\< "&lt;", \> "&gt;", \& "&amp;"}))


(defn clean-form-data [form-data]
  {:url (escape-string (get form-data :link))
   :user_id (get-user-id)
   :title (escape-string (get form-data :link-name))
   :desc (escape-string (get form-data :desc))
   :image_url (escape-string (get form-data :image-url))})

(def sample-form-data
  {:url "https%3A%2F%2Fgithub.com%2Fring-clojure%2Fring-codec"
   :user_id 1
   :title "This is supposed to be the url title"
   })


(defn get-form-data [req]
  (get req :params))

; check if link is the same link is already present in the database
(defn is-url-already-present [url]
  (log/info "insise is-url-already-present")
  (< 0  (get (first
               (db/get-url-count {:url url}))
             :count)))

(defn insert-link! [req]
  (if (not (is-url-already-present (:link (:params req))))
    (do 
      ;(log/info "get-link-details >>>>>> " (merge (clean-form-data (get-form-data req)) (preview/get-link-details (:link (:params req)))))
      (db/insert-link<! (clean-form-data (get-form-data req))))
    {:error "URL already present"}))

 ;(defn insert-link! [request] 
  ; (encode-url (get-in request [:params :link])))


(defn get-link-preview [url]
  (preview/get-link-details url))


; get all links from database
(defn get-links [req]
  ;(doseq [l (db/get-links)]
  ;  (timbre/info (preview/get-link-details  (:url  l))))
  (log/info "req map in get-links: " req)
  (db/get-links))


