(defproject food-closure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [clj-http "2.3.0"]
                 [org.clojure/data.json "0.2.6"]
                 [enlive "1.1.6"]
                 [org.clojure/java.jdbc "0.7.0-alpha1"]
                 [com.h2database/h2 "1.4.193"]
                 ]
  :main ^:skip-aot food-closure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
