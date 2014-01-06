(defproject pitbull "0.1.2"
  :description "Clojure Protocol Buffers"
  :url "https://github.com/samn/pitbull"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :test-paths ["test/clj"]
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test/"]
  :uberjar-exclusions [#"test/"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]] :plugins [[lein-midje "3.1.1"]]}
             ;; Add an explicit dependency on Clojure in the production profile for perf tests
             :production {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :dependencies [[com.google.protobuf/protobuf-java "2.5.0"]
                 [potemkin "0.3.4"]
                 [criterium "0.4.2" :scope "test"]
                 [midje "1.5.1" :scope "test" :exclusions [org.clojure/clojure]]])
