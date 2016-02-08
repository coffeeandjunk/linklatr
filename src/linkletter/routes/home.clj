(ns linkletter.routes.home
  (:require [linkletter.layout :as layout]
            [compojure.core :refer [defroutes GET POST ANY]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as res]
            [liberator.core :refer  [defresource resource]]
            [cheshire.core :refer  [generate-string parse-string]]
            [linkletter.home-handler :as home]
            [linkletter.utils.login :as login]
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
  (log/info "\n \n session data>>>>> : request: " (:sesison res/response))
  (let [op (home/insert-link! request)]
    (if-not (contains? op :error)
      (do ( log/info "from submit-link: " op)
          (json-response op))
      (json-response {:data op} 400)))) 


(defn home-page []
  ;(layout/render "home.html")
  (res/redirect "index.html"))

(defn get-links [req]
  (json-response (home/get-links req)))


(defn home
  [request]
  ;(if-not (authenticated? request)
    ;(throw-unauthorized)
    ;(okay {:status "Logged" :message (str "hello logged user "
                                        ;(:identity request))}))
  (layout/render "home.html")
  )

(defn login-page
  []
  ;(res/redirect "login.html")
  (layout/render "login.html")
  )

(defn auth
  [req]
  ;(layout/render "home.html")
  ;(log/info "\n\n FB code:>>>>>>> " (str (:params req)))
  (login/authenticate (:params req))
  ;(login/set-user! req {:user "user-data"})
  
  (assoc (res/redirect "/home")
         :session (assoc (:session req) :userid "test id")
         )
  
  )

(defn login
  [request]
  (log/info "\n \n login data" (str  (:params request)))
  ;; if uesr is valid, if not throw error in ui
  ;; register in db if new user, then get user data and redirect user to home page
  (if (login/validate-user? (:params request))
    ;;(log/info "\n\n Profile data: " (login/register-user (login/get-profile-data (:params request))))
    (do  
      (res/response (assoc (:session request) :user 4))
      (json-response (login/get-user-data (login/register-user (login/get-profile-data (:params request))))))
    (json-response {:error "Error in loggin in. Please try again"})))


(defn get-preview-details
  "fetches the url details"
  [request]
  (log/info ">>>> query-params>>>> " (keywordize-keys  (:query-params request)))
  (json-response (home/get-link-preview (:url  (keywordize-keys (:query-params request))))))


(defroutes home-routes
  (GET "/" [] (home))
  (GET "/links" [request] get-links)
  (GET "/home" [request] home)
  (POST "/"  [] submit-link)
  (GET "/login" [] (login-page)) 
    (GET "/link/details*" [request] get-preview-details)
  ;(POST "/login" [request] login)
  (GET "/auth" [] auth)
  )

