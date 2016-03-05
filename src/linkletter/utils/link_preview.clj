(ns linkletter.utils.link-preview
  (:require [linkletter.db.core :as db]
            [ring.util.http-response :refer [ok]]
            [ring.util.codec :as codec]
            [clojure.string :as cstring]
            [net.cgrand.enlive-html :as html]
            [clj-http.client :as client]
            [slingshot.slingshot :as sling :only [throw+ try+]]
            [taoensso.timbre :as log]
            [cheshire.core :refer  [generate-string]])
  (:import [org.apache.commons.lang3 StringEscapeUtils]))

(def config-options
  "header for the get request"
  {:headers { :User-Agent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0"
             :Accept "text/html,application/x"
             :Connection "keep-alive" } 
   :max-redirects 5
   :throw-exceptions true 
   :insecure? true })

(defn handle-http-exceptions
  "handles clj-http errors"
  [request-time headers body & msg]
  (log/warn (str "http exception. Error: " headers))
  msg)

;(getHost (.getHost java.net.URL. "http://www.example.com/docs/resource1.html"))


(defn get-meta-tag-content
  [tag-content]
  (-> tag-content
      first
      :attrs
      :content))

(defn fetch-url 
  "fetches the webpage and returns enlive nodes"
  [url]
  (sling/try+
    (html/html-resource (java.io.StringReader. (:body  (client/get url config-options))))
    (catch [:status 404] {:keys [request-time headers body]}
      (handle-http-exceptions request-time headers body "404"))))


(defn get-elements-by-tag [page tag] 
  (html/select 
    (html/html-resource page) [(keyword tag)]))


(defn get-item-by-property 
  "gets elements by the provided attr-name or 'property' by default"
  ([page value] (get-item-by-property page value "property"))
  ([page value attr-name]
   (html/select-nodes*  page [(html/attr= (keyword attr-name) value)])))

(defn get-title 
  "Gets the page title, from fb meta tag or title tag"
  [page]
  (let [fb-titile  (get-meta-tag-content (get-item-by-property page "og:title"))]
    (if-not (nil? fb-titile)
      fb-titile
      (first (:content (first (get-elements-by-tag page "title")))))))

(defn get-host-url
  "returns the host url of the provied url"
  [url]
  (.getHost (java.net.URL. url)))

(defn url-or-nil
  "return a java.net.URL instance or nil if failed to instantiate"
  [url]
  (try
    (java.net.URL. url)
    (catch java.net.MalformedURLException e nil)))

(defn remove-starting-slash
  "removes the starting slash form the string"
  [path]
  (if (.startsWith (.toString path) "/")
    (subs path 1)
    path))

(defn form-valid-url 
  "returns a valid url if the string is a relative path, by appending the host and protocol from the url"
  [path url]
  (if-not (url-or-nil path)
    (do (let [url-instance (java.net.URL. url)
              rel-path (remove-starting-slash path)]
          (str (.getProtocol url-instance) 
               "://"
               (.getHost url-instance)
               "/"
               rel-path)))
    path))

(defn get-image-url
  "Gets the url for fb meta tag image or the first image of the page"
  [page url]
  (or (get-meta-tag-content (get-item-by-property page "og:image"))
      (-> (get-elements-by-tag page "img")
          first
          :attrs
          :src
          (form-valid-url url))))


(defn get-desc-from-meta [page]
  (get-meta-tag-content (get-item-by-property (get-elements-by-tag page "meta")
                                                 "description"
                                                 "name")))
(defn get-url-from-meta
  "gets the url from og:url meta tag"
  [page]
  (get-meta-tag-content (get-item-by-property page "og:url")))

(defn get-desc-from-body
  "gets page description from the text nodes in the body, only 200 characters"
  [page]
(->> 
  (-> (cstring/join "" (html/select page [:body html/text-node]))
      (cstring/replace #"\n|\n\r|\t" " ")
      cstring/trim
      (cstring/split #" "))
  (take 200)
  (filter #(not ( = "" %)))
  (cstring/join " ")))

(defn escape-html
  "Escapes the characters in a String using HTML entities"
  [str-content]
  (org.apache.commons.lang3.StringEscapeUtils/escapeHtml4 str-content))

(defn get-desc
  "Gets page description"
  [page]
  (let [fb-desc (get-meta-tag-content (get-item-by-property page "og:description"))]
    (cond (not (nil? fb-desc)) fb-desc
          (not (nil? (get-desc-from-meta page))) (get-desc-from-meta page) 
          :else (get-desc-from-body page))))

(defn get-link-details
  "returns a map with url, title, image-url and link description"
  [url]
  (let [page (fetch-url url)]
    {:title (escape-html (get-title page))
     :image_url (escape-html (get-image-url page url))
     :desc (escape-html (get-desc page)) 
     :url (escape-html (or (get-url-from-meta page) url))}))


