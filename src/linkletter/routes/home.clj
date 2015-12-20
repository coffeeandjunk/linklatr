(ns linkletter.routes.home
  (:require [linkletter.layout :as layout]
            [compojure.core :refer [defroutes GET POST ANY]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as res]
            [liberator.core :refer  [defresource resource]]
            [cheshire.core :refer  [generate-string parse-string]]
            [linkletter.home-handler :as home]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [buddy.sign.jws :as jws]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as log]
            [clojure.walk :refer [keywordize-keys]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))


(def users (atom  ["foo" "bar" "moo"]))
(def secret "mysupersecret")

(def json-map {"url" "test url"})
(def clj-map {:a 1 :b 3})
(defn tt []
  (generate-string clj-map))
(defn tt1 []
  (parse-string json-map))

(defn okay [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (generate-string data)})

(defn submit-link [request]
  (log/info "submit-link : request: " request)
  (let [op (home/insert-link! request)]
    (if-not (contains? op :error)
      (do ( log/info "from submit-link: " op)
          (json-response op))
      (json-response {:data op} 400)))) 


;(defn get-user [id]
 ; (db/get-user {:id id}))

;(defresource get-users
;  :allowed-methods  [:post]
;  :handle-ok  (fn  [_]  (str "this is a sample text")) 
;  :available-media-types  ["text/plain"])

(defn home-page []
  ;(layout/render "home.html")
  (res/redirect "index.html"))

(defn get-links []
  (json-response (home/get-links)))


(defn home
  [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (okay {:status "Logged" :message (str "hello logged user "
                                        (:identity request))})))


(def authdata {:admin "secret"
               :test "secret"})


(defn login-page
  []
  (res/redirect "login.html"))

(defn login
  [request]
  (let [username (get-in request [:params :username])
        password (get-in request [:params :password])
        valid? (some-> authdata
                       (get (keyword username))
                       (= password))]
    (println "*****request object****  " request)
    (println "inside login funciton " username password)
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds 3600))}
            token (jws/sign claims secret {:alg :hs512})]
        (okay {:token token}))
      (bad-request {:message "wrong auth data"}))))

(defn about-page []
  (layout/render "about.html"))

(defn get-preview-details
  "fetches the url details"
  [request]
  (log/info ">>>> query-params>>>> " (keywordize-keys  (:query-params request)))
  (json-response (home/get-link-preview (:url  (keywordize-keys (:query-params request)))))
  
  )


(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/links" [] (get-links))
  (GET "/home" [request] home)
  (GET "/link/details*" [request] get-preview-details)
  (POST "/"  [] submit-link)
  (GET "/login" [] (login-page))
  (POST "/login" [request] login)
  ;(ANY "/users" request get-user)
  (GET "/about" [] (about-page)))

