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

(defn url-present-in-db? 
  "Check if the url is already added by some user"
  [url]
  (log/debug "inside home_handler/url-present-in-db?")
  ;(< 0  (get (first (db/get-url-count {:url url})) :id))
  (-> {:url url}
      db/get-url-count
      first
      (get :id)))

;(def url "http://firstround.com/review/this-company-retains-95-percent-of-its-employees-heres-its-secret/?ct=t%28How_Does_Your_Leadership_Team_Rate_12_3_2015%29")
;(url-present-in-db? url)


(defn update-user-link-db! 
  "inserts into l2u table the link id and the corresponding user-id"
  [{:keys [id user_id]}]
  (log/debug (str "from insert-into-l2u<! id: " id "  user_id: " user_id))
  (:id (db/insert-link2user<! {:lid id :uid user_id})))

;(defn insert-into-u2l<!
  ;"inserts into u2l table"
  ;[{:keys [lid uid]}]
  ;(db/insert-user2link<! {:uid uid :lid lid})
  ;)

(defn link-mapped-to-user?
  "check if the given link-id is already mapped to the given user"
  [link-id user-id]
  (log/debug "from mapped-to-user? link-id user-id :" link-id user-id)
  (-> {:lid link-id :uid user-id}
      db/get-url-mapping-count
      first
      (get :count)
      (> 0)))

;(mapped-to-user? 87 14)

; TODO try to further simplyfy the insert function
(defn insert-link! 
  "inserts link to db if new link or else just maps the existing link to the current user, returns link id"
  [req]
  (log/debug "from home_handler/insert-link! :params " (:params req))
  (let [user-id (get-user-id req)]
    (if-let [link-id (url-present-in-db? (get-in req [:params :link]))]
      (if (link-mapped-to-user? link-id user-id) 
        (do  
          (log/debug " already mapped to the user")
          {:error "You have alredy collected this link" :link-id link-id})        
        (do  
          (log/debug "inside second else block link-id user-id: " link-id user-id)
          (update-user-link-db! {:id link-id  :user_id user-id})
          (db/get-link-details {:lid  link-id})))
      (do 
        (log/debug "from insert-link! else block")
        (let [link-data (db/insert-link<! (assoc (clean-form-data (get-form-data req)) 
                                                 :user_id user-id))]
          (update-user-link-db! {:id (:id link-data) :user_id user-id})  
          link-data)))))

(defn get-link-preview [url]
  (preview/get-link-details url))


(defn get-user-link-count
  "fetches the total number of links for the currrent user"
  [user-id]
  (-> (db/get-user-link-count {:user_id user-id})
      first
      :count))


; TODO change the function after modifiying the schema
; TODO modify the function to accept user-id instead of req map
(defn get-links 
  "Fetches  links for the current user"
  [req]
  (let [user-id (get-user-id req)
        limit (Integer/parseInt (get-in req [:params :limit])) 
        offset (Integer/parseInt (get-in req [:params :offset]))
        initial-call (boolean (get-in req [:params :initial-call]))
        link-count (get-in req [:session :user-links-count])]
    (log/info (str "from home_handler/get-links user-id:" user-id " :limit" limit " :offset" offset " :inital-call" initial-call))
    (when (< offset link-count)
      ;(assoc (db/get-links  {:user_id user-id :limit limit :offset offset}) :link-count link-count)
      {:result-set (vec (db/get-links  {:user_id user-id :limit limit :offset offset})) :link-count 31}))) 



(defn delete-link-from-db!
  "deletes a link from the db permanently"
  [link-id]
  (db/delete-link! {:lid link-id})
  )

(defn link-mapped-to-any-user?
  "returns if the link-id is mapped to a user"
  [link-id]
  (-> {:lid link-id}
      db/link-map-count
      first
      (get :count)
      (> 0)))

;(link-mapped-to-any-user? 77)
;(db/link-map-count {:lid 77})


(defn delete-link!
  "removes mapping between the given user-id and the given link-id, retains the link if some user is still using it"
  [link-id user-id]
  (let 
    [l2u-delete-count (db/delete-mapping-for-link! {:lid link-id :uid user-id})]
    (when (not (link-mapped-to-any-user? link-id))
      (do (log/info "from home_handler/delete-link! when block" link-id) 
          (delete-link-from-db! link-id)))))

;(db/search {:? [(str "life")]})

(defn search
  "Searches the title and description for the given string. If the search term consists of more than one word, the words are joined by logical AND"
  [query-map]
  (let [result-set (vec (db/search {:? [(str (:q query-map))]}))]
    {:result result-set :count (count result-set)}))

;(search "life")
