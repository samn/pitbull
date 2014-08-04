(defproject pitbull "0.1.3-SNAPSHOT"
  :description "A Map like Clojure API for Protocol Buffers"
  :url "https://github.com/samn/pitbull"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :test-paths ["test/clj"]
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test/"]
  :uberjar-exclusions [#"test/"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]]
                   :plugins [[lein-midje "3.1.3"]]}
             ;; Add an explicit dependency on Clojure in the perf profile for performance tests
             :perf {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :dependencies [[com.google.protobuf/protobuf-java "2.5.0"]
                 ;; later versions of potemkin don't support Clojure 1.4
                 ;; 1.4 support is required for use with Storm
                 [potemkin "0.3.4"]
                 [criterium "0.4.2" :scope "test"]
                 [midje "1.6.3" :scope "test" :exclusions [org.clojure/clojure]]])
