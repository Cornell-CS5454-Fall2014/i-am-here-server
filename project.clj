(defproject i-am-here "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [korma "0.3.3"]
                 [com.h2database/h2 "1.3.172"]
                 [clj-time "0.8.0"]
                 [ring/ring-json "0.3.1"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler i-am-here.handler/app
         :init i-am-here.handler/init
         :port 8080}
  :profiles
  {:dev {:dependencies [
                        [javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
