(ns linkletter.routes.facebook
 (:use compojure.core)
 (:require [clj-oauth2.client :as oauth2]
           [cheshire.core :as parse]))


(def facebook-oauth2
  {:authorization-uri "https://graph.facebook.com/oauth/authorize"
   :access-token-uri "https://graph.facebook.com/oauth/access_token"
   :redirect-uri "http://localhost:3000"
   :client-id "1234567890"
   :client-secret "2a34196385c9c3d0695f05cf9f3ca2c7"
   :access-query-param :access_token
   :scope ["user_photos" "friends_photos"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def auth-req
  (oauth2/make-auth-request facebook-oauth2))


;; auth-resp is a keyword map of the query parameters added to the
;; redirect-uri by the authorization server
;; e.g. {:code "abc123"}
;(def access-token
 ; (oauth2/get-access-token facebook-oauth2 auth-resp auth-req))

;; access protected resource
;(oauth2/get "https://graph.facebook.com/me" {:oauth2 access-token})
