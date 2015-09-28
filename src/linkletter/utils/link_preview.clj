(ns linkletter.utils.link-preview
  (:require [linkletter.db.core :as db]
            [ring.util.http-response :refer [ok]]
            [ring.util.codec :as codec]
            [clojure.string :as cstring]
            [net.cgrand.enlive-html :as html]
            [clj-http.client :as client]
            [cheshire.core :refer  [generate-string]]))

(def headers
  "header for the get request"
  {:headers { :User-Agent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0"
             :Accept "text/html,application/x"
             :Connection "keep-alive" } 
   :max-redirects 5
   :throw-exceptions true })
(def url "http://www.99points.info/2010/07/facebook-like-extracting-url-data-with-jquery-ajax-php/")

(defn fetch-url 
  "fetches the webpage and returns enlive nodes"
  [url]
  ;(html/html-resource (java.io.StringReader. (slurp  "http://www.99points.info/2010/07/facebook-like-extracting-url-data-with-jquery-ajax-php")))
  ;(html/html-resource (java.io.StringReader  (client/get url headers)))
  (html/html-resource (java.io.StringReader. (:body  (client/get url headers))))
  ;(spit (slurp "http://aeon.co/magazine/science/the-universal-constants-that-drive-physicists-mad/")
  ;(html/html-resource (java.io.StringReader. (slurp "test.html"))) 
  )

;(html/html-resource (java.io.StringReader. (fetch-url)))


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
  (let [fb-titile  (:content (:attrs (first (get-item-by-property page "og:title"))))]
    (if-not (nil? fb-titile)
      fb-titile
      (first (:content (first (get-elements-by-tag page "title")))))))

(defn get-image-url
  "Gets the url for fb meta tag image or the first image of the page"
  [page]
  (let [fb-img (:content (:attrs (first (get-item-by-property page "og:image"))))]
  (if-not (nil? fb-img)
    fb-img
    (:src  (:attrs  (first 
                      (get-elements-by-tag page
                                           "img")))))))
(defn get-desc-from-meta [page]
  (:content (:attrs (first (get-item-by-property (get-elements-by-tag page "meta")
                                                 "description"
                                                 "name")))))
(defn get-desc-from-body
  "gets page description from the tags in the body, only 80 characters"
  [page]
  (cstring/join " " 
                (take 80 (cstring/split (cstring/trim
                                          (cstring/replace  (cstring/join ""
                                                                          (html/select page 
                                                                                       [:body html/text-node]))
                                                           #"\n|\n\r|\t"
                                                           " "))
                                        #" "))))
(defn get-desc
  "Gets page description"
  [page]
  (let [fb-desc (:content (:attrs (first (get-item-by-property page "og:description"))))]
    (cond (not (nil? fb-desc)) fb-desc
          (not (nil? (get-desc-from-meta page)))(get-desc-from-meta page) 
          :else (get-desc-from-body page))))


  
  ;(filter #(max (count %)) (html/texts (get-elements-by-tag  (fetch-url) "div")))
  

;(get-title (fetch-url))

;(get-image-url (fetch-url))

(defn get-link-details
  "returns a map with title, image-url and link description"
  [url]
  (let [page (fetch-url url)]
    {:title (get-title page)
     :image-url (get-image-url page)
     :desc (get-desc page) }))


;(get-link-details "http://clojure.org/cheatsheet")
;(.getHost (java.net.URL. "http://clojure.org/cheatsheet"))

