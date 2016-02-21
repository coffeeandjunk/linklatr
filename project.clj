(defproject linkletter "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [selmer "0.8.9"]
                 [com.taoensso/timbre "4.1.1"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.68"]
                 [environ "1.0.0"]
                 [compojure "1.4.0"]
                 [liberator "0.13"]
                 [cheshire "5.5.0"]
                 [lib-noir "0.9.9"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-ttl-session "0.1.1"]
                 [ring "1.4.0"
                  :exclusions [ring/ring-jetty-adapter]]
                 [ring/ring-codec "1.0.0"]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.3"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.webjars/bootstrap "3.3.5"]
                 [org.webjars/jquery "2.1.4"]
                 [buddy "0.6.2"]
                 [clj-time "0.11.0"]
                 [migratus "0.8.4"]
                 [conman "0.1.6"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [yesql "0.5.2"]
                 [stuarth/clj-oauth2 "0.3.2"]
                 [enlive "1.1.6"]
                 [slingshot "0.12.2"]
                 [clj-http "2.0.0"]
                 [org.immutant/web "2.0.2"]]

  :min-lein-version "2.0.0"
  :uberjar-name "linkletter.jar"
  :jvm-opts ["-server"]

  :main linkletter.core
  :migratus {:store :database}

  :plugins [[lein-environ "1.0.0"]
            [migratus-lein "0.1.7"]]

  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev  {:dependencies [[ring/ring-mock "0.2.0"]
                                 [ring/ring-devel "1.4.0"]
                                 [pjstadig/humane-test-output "0.7.0"]
                                 [mvxcvi/puget "0.8.1"]]
                  
                  
                  :repl-options {:init-ns linkletter.core}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]
                  ;;when :nrepl-port is set the application starts the nREPL server on load
                  :env {:dev        true
                        :port       3000
                        :nrepl-port 7000}}
   :project/test {:env {:test       true
                        :port       3001
                        :nrepl-port 7001}}
   ;:profiles/dev {}
   ;:profiles/test {}
   })
