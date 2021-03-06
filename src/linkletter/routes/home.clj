(ns linkletter.routes.home
  (:require [linkletter.layout :as layout]
            [compojure.core :refer [defroutes GET POST DELETE PATCH ANY]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as res]
            ;[liberator.core :refer  [defresource resource]]
            [cheshire.core :refer  [generate-string parse-string]]
            [linkletter.home-handler :as home]
            [linkletter.utils.login :as login]
            [clojure.java.io :as io]
            [clj-time.core :as time]
            [buddy.sign.jws :as jws]
            [environ.core :refer [env]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as log]
            [clojure.walk :refer [keywordize-keys]]
            [ring.middleware.session :as ring-session]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))




(defn okay [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (generate-string data)})

(defn submit-link [request]
  (log/info "from home/submit-link session data>>>>> : request: " (:sesison res/response))
  (let [op (home/insert-link! request)]
    (if-not (contains? op :error)
      (do ( log/info "from submit-link: " op)
          (json-response op))
      (json-response {:data op} 400)))) 


(defn set-session-value
  "sets the map in session"
  [map-key map-value request response]
  (assoc response 
         :session (assoc (:session request) map-key map-value)))


(defn get-links [req]
  ;(set-viewed-links-count-in-session req)
   (json-response (home/get-links req)))

(defn home
  [request]
    (log/debug "from home/home :session " (:session request))
    (if (login/user-logged-in? request)
      (do (log/debug "from home/home user logged in") 
          (set-session-value
            :user-links-count (home/get-user-link-count (home/get-user-id  request))
            request  
            (layout/render "home.html")
            ;(res/redirect home)
            ))
      (do (log/debug "from home/home user not logged in")
          (res/redirect "/login"))))

(defn home-page
  "redirects to homepage if the user is logged in, else redirects to login page"
  [request]
  (log/debug "from home/home-page link-count" (home/get-user-link-count (home/get-user-id  request)))
  (if (login/user-logged-in? request)
    (set-session-value
      :user-links-count (home/get-user-link-count (home/get-user-id  request))
      request  
      (res/redirect home))))

(defn obs-link
  "Obscure link"
  [req]
  ;; sets a session with the hard-coded user details
  (let [ojana-user {:email "oajana.user@gmail.com", :first_name "Ojana", :admin nil, :id 5, :last_name "User", :pass nil}]
    (assoc (res/redirect "/home")
           :session (assoc (:session req) :profile-data ojana-user)))
  ;(layout/render "home.html")
  )

(defn login-page
  [request]
  ;(res/redirect "login.html")
  (log/info (str "from home/login-page, params passed to login-page: " 
                 {:redirect-url (env :redirect-url)
                  :scope "public_profile,email"
                  :client-id (env :fb-app-id)}))
  (if (not  (login/user-logged-in? request))
    (layout/render "login.html" {:redirect-url (env :redirect-url)
                                 :scope "public_profile,email"
                                 :client-id (env :fb-app-id)})
    (res/redirect "/home")))


(defn logout
  [req]
  (assoc (res/redirect "/login")
         :session nil))

(defn auth
  [req]
  ; TODO check if the permission is granted from FB
  ; if permission granted > authenticate user > set-session > redirect to /home
  ; :else redirect to login
  (let [query-params (:params req)]
    (if (and  (contains? query-params :code) (not (nil? (:code query-params))))
      (do 
        (log/debug "From login/auth params contain code" (get-in req [:params :code]))
        (let [access-token (:access_token (login/get-fb-access-token (get-in req [:params :code])))
              user-id (login/get-fb-user-id access-token)
              profile-data (-> (login/get-profile-data {:user-id user-id
                                                        :access-token access-token})
                               login/register-user
                               login/get-user-data)]
          (assoc (res/redirect "/home")
                 :session (assoc (:session req) :profile-data profile-data)))) 
      (do 
        (log/info "\n From login/auth Permission not granted")      
        (assoc (res/redirect "/login")
               :session nil)))))

(defn login
  [request]
  (log/info "\n \n login data" (str  (:params request)))
  ;; if uesr is valid, if not throw error in ui
  ;; register in db if new user, then get user data and redirect user to home page
  (if (not (login/user-logged-in? request))
    (json-response (login/get-user-data (login/register-user (login/get-profile-data (:params request)))))
    (res/redirect "/home")
    )
  ;(if (login/validate-user? (:params request))
  ;  (do  
  ;   (json-response (login/get-user-data (login/register-user (login/get-profile-data (:params request))))))
  ;  (json-response {:error "Error in loggin in. Please try again"}))
  )

(defn about-page
  [req]
  (res/redirect "about.html"))


(defn get-preview-details
  "fetches the url details"
  [request]
  (log/debug "from home/get-preview-details request: " request)
  (json-response (home/get-link-preview (:url  (keywordize-keys (:query-params request))))))

(defn handle-delete
  "deletes the link for the given id for the current user"
  [req]
  (let [user-id (home/get-user-id req)
        ; TODO fix this, get the link-id from the path
        link-id (Integer/parseInt (:id (:params req)))
        ]
  (log/info  (str "form delte-link user-id link-id " (class user-id) (class link-id)))
  (home/delete-link! link-id user-id))
  {:status 204})

(defn handle-link
  "inserts new link submitted through the extension"
  [req]
  ;(log/info "from home/handle-link " req)
  (let [profile-data {:id  (Integer/parseInt (get-in req [:params :user-id]))}]
    (home/insert-link! (assoc  req 
                              :session {:profile-data profile-data}))
     (log/info "profile-data" profile-data)
    (json-response {:success true})))

(defn handle-search
  "queries the search term, returns the json for the search result and the count"
  [req]
  (log/info (str "from home/handle-search" (:query-params req)))
  (if (login/user-logged-in? req)
    (do  (-> (:query-params req)
             keywordize-keys
             home/search 
             json-response))
    (res/redirect "/login")))

(defn search-page
  "renders the search page"
  [req]
  (log/info (str "from home/search-page" ))
  (if (login/user-logged-in? req)
    (layout/render "search.html")
    (res/redirect "/login")))



(defroutes home-routes
  (GET "/" [request] login-page)
  (GET "/links" [request] get-links)
  (GET "/home" [request] home)
  (POST "/"  [] submit-link)
  (DELETE "/link/:id"  [] handle-delete)
  (GET "/login" [request] login-page) 
  (GET "/logout" [] logout) 
  (GET "/link/details*" [request] get-preview-details)
  (GET "/ojanalink" [request] obs-link) ;; obscure link
  (GET "/about" [] about-page) ;; obscure link
  (GET "/auth" [] auth)
  (GET "/search" [request] search-page)
  (GET "/search/q*" [request] handle-search)
  ;; routes for extension
  ;; TODO group them together
  (POST "/link" [] handle-link)
  )

