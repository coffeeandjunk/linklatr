(ns linkletter.home-handler
  (:require [linkletter.db.core :as db]
            [ring.util.codec :as codec]
            [taoensso.timbre :as log]
            [linkletter.utils.link-preview :as preview]
            [cheshire.core :refer  [generate-string]]))



(defn encode-url [url]
  (codec/url-encode url))

(defn decode-url [url]
  (codec/url-decode url))

; user id is hard-coded for now
(defn get-user-id 
  "gets user id from the session"
  [req]
  (log/debug "from home_handler/get-user-id request: " req)
  (get-in req [:session :profile-data :id]))

(defn escape-string [str]
  (clojure.string/escape str {\< "&lt;", \> "&gt;", \& "&amp;"}))


(defn clean-form-data [form-data]
  {:url (escape-string (get form-data :link))
   ;:user_id (get-user-id)
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
  (log/info "inside is-url-already-present")
  (< 0  (get (first
               (db/get-url-count {:url url}))
             :count)))

(defn update-user-link-tables
  "upadtes u2l and l2u tables for link and user mappings"
  [{:keys [user-id link-id]}]
  1)

(defn insert-into-l2u<!
  "inserts into l2u table the link id and the corresponding user-id"
  [{:keys [id user_id]}]
  (log/debug (str "from insert-into-l2u<! id: " id "  user_id: " user_id))
  (db/insert-link2user<! {:lid id :uid user_id}))

;(defn insert-into-u2l<!
  ;"inserts into u2l table"
  ;[{:keys [lid uid]}]
  ;(db/insert-user2link<! {:uid uid :lid lid})
  ;)


(defn insert-link! [req]
  (log/debug "from home_handler/insert-link! :params " (:params req))
  (if (not (is-url-already-present (:link (:params req))))
    (do 
      (let [form-data (clean-form-data (get-form-data req))
            user-id (get-user-id req)]
      (log/debug "from insert-link! user-id::" form-data)
      ;(let [link-data (db/insert-link<! (assoc form-data :user_id user-id))]) 
      (->> (db/insert-link<! (assoc form-data :user_id user-id))
          (insert-into-l2u<!) 
          (log/debug "from home_handler/insert-link! inset-into-l2u<! data: " ))
      ))
    {:error "URL already present"}))

 ;(defn insert-link! [request] 
  ; (encode-url (get-in request [:params :link])))


(defn get-link-preview [url]
  (preview/get-link-details url))


; get all links from database
; TODO change the function after modifiying the schema
; TODO modify the function to accept user-id instead of req map
(defn get-links 
  "gets all links for the current user"
  [req]
  (let [ user-id (get-user-id req)]
  (log/info "from home_handler/get-links user-id: " user-id)
  (db/get-links  {:user_id user-id})))

;(db/get-links {:user_id 14})


