(ns linkletter.utils.login
  (:require [linkletter.db.core :as db]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :as res]
            [linkletter.db.core :as db]
            [ring.util.codec :as codec]
            [taoensso.timbre :as log]
            [linkletter.utils.link-preview :as preview]
            [clj-http.client :as client]
            [slingshot.slingshot :as sling :only [throw+ try+]]
            [cheshire.core :refer  [generate-string parse-string]]))

;; TODO set these data in the env
(def fb-app-id "826503424132665")
(def fb-app-secret "2a34196385c9c3d0695f05cf9f3ca2c7")
(def fb-api-url "https://graph.facebook.com/v2.5/")
(def fb-userid-url "https://graph.facebook.com/v2.5/me?access_token=")
(def redirect-url "http://localhost:3000/auth") ;TODO set it from env variables for production
;(def redirect-url "http://linkletter.co/auth") 


(def fb-token-url 
  (str "https://graph.facebook.com/v2.3/oauth/access_token?client_id=" fb-app-id "&redirect_uri=" redirect-url "&client_secret=" fb-app-secret "&code="))

(defn get-graph-api-url [id access-token]
  (str fb-api-url id "/?access_token=" access-token "&fields=id,name,email,picture, first_name, last_name" ))

(defn get-fb-user-id
  "returns the fb userid for the given access token"
  [access-token] 
  (log/debug "from get-fb-user-id access-token " access-token)
  (-> (str fb-userid-url access-token)
      (client/get  {:accept :json})
      :body
      (parse-string true)
      :id))


(defn get-fb-access-token
  "fetches the token from FB given the code, that was generated during signin, check https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow#exchangecode for more info"
  [code]
  (-> (str fb-token-url code)
      (client/get  {:accept :json})
      :body
      (parse-string true)))
;(get-fb-access-token "AQB_v9pd1YbCkRw-f-SBtPIyh4XOHNRFd_t838PjyclKi12GHcVzWzfkU-h5jM-PcCz-uGFQyCF34_TJEu9jtcGyOr0vly_DN6mpO2uV5TkN_PnmQ-a6G_Ir7MJyiu-HgNltwXvk5cKeRbfYM2x1IQA_oveCvb3_SmjsKjCksvzgWTmCMSuLehc9F5fyju9BZR49OGUQq0XGEQaiJNkgjTOs1i-eZi9YbptXtFHgjpCMOw7z3KCfiLPAR0F_JZqfsbvw_l9K19-DoPnbUkgItdxIGPV8ZxNMa392lrf3fxSVTMKhhkZ6zoavDGccKPJnMks")

(defn get-debug-token-url [access-token]
  (str "https://graph.facebook.com/debug_token?input_token="
       access-token
       "&access_token="
       fb-app-id "|" fb-app-secret))

(defn get-token-debug-data [accessToken]
  (try
    (parse-string (:body (client/get (get-debug-token-url accessToken) {:accept :json})) true)
    (catch Exception e (log/error (str "Message: "  (.getMessage e) (.toString e))))))


(defn validate-user? [req-data];[{:keys [accessToken userID]}]
  (log/info "validate-user? >>>>> \n" req-data)
  (try
  (let [user-id (:userID req-data)
        access-token (:accessToken req-data)
        debug-token-response (get-token-debug-data access-token)]
    (log/info "from logn/validate-user? debug token response: " debug-token-response)
    (if (and (= fb-app-id (:app_id (:data debug-token-response)))
             (= user-id (:user_id (:data debug-token-response))))
      true
      false))))

(defn user-logged-in?
  "fetch tooken from fb and authenticate user"
  [request]
  (log/debug "from user-logged-in? :profile-data" (get-in request [:session :profile-data]))
  (boolean (get-in request [:session :profile-data])))


(defn set-user!
  [req user-data]
  ;(assoc (:session req) :session (assoc :session :user "this is a test id"))
  (assoc (res/response "session is set") :session (assoc (:session req) :user "this is a test"))
  )

(defn get-profile-data
  "fetches profile data from the social networking site, currently from fb"
  [req]
  (let [user-id (:user-id req)
        access-token (:access-token req)
        profile-data (parse-string (:body (client/get (get-graph-api-url user-id access-token) {:accesp :json})) true)]
    (log/info "from login/get-profile-data profile-data: " profile-data)
    {:firstname (:first_name profile-data)
     :lastname (:last_name profile-data)
     :email (:email profile-data)
     :profile_pic (:url (:data (:picture profile-data)))}))


(defn check-if-new-user?
  [email]
  (if (> 1 
         (get (first (db/get-user-count {:email email}))
              :count))
    true
    false))

(defn get-user-id
  [email]
  (log/info "\n\n  From get-user-id, userID " (first (db/get-user-id {:email email})))
  (first (db/get-user-id {:email email}))
  )

(def sample-profile
 {:firstname "Chinmoy", :lastname "Debnath", :email "chinmoy.debnath11@gmail.com", :profile_pic "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xtp1/v/t1.0-1/p50x50/10639606_10206960943255682_7537067390031861946_n.jpg?oh=638868385488e682c99e1bac94435236&oe=56:FB3A58&__gda__=1460982164_c471e2cfc91aa68dba92bd0c7f636da4"})

(defn register-user
  "register the user if she is a new user, else just return the userid"
  [profile-data]
  (log/info "\n\n request in register user function" profile-data)
  (if (check-if-new-user? (:email profile-data))
    (:id (db/insert-user<! profile-data))
    (:id  (first (db/get-user-id {:email (:email profile-data)})))))



(defn get-user-data [user-id]
  (log/info "user-id in get-user-data >>>>>>> " user-id)
  (first (db/get-user-data {:id  user-id})))

;(register-user sample-profile )
;(check-if-new-user? (:email  sample-profile))
;(def email (:email sample-profile))
;(def profile-data sample-profile)
