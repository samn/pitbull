(defproject pitbull "0.1.3"
  :description "A Map like Clojure API for Protocol Buffers"
  :url "https://github.com/samn/pitbull"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :test-paths ["test/clj"]
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test/"]
  :uberjar-exclusions [#"test/"]
  :profiles {:dev [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :test {:plugins [[lein-midje "3.1.3"]]
                    :dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]
                                   [criterium "0.4.2"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :dependencies [[com.google.protobuf/protobuf-java "2.5.0"]
                 ;; later versions of potemkin don't support Clojure 1.4
                 ;; 1.4 support is required for use with Storm
                 [potemkin "0.3.4"]])
