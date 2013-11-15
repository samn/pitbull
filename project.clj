(defproject pitbull "0.1.0"
  :description "Clojure Protocol Buffers"
  :url "https://github.com/samn/pitbull"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :test-paths ["test/clj"]
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test/"]
  :uberjar-exclusions [#"test/"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  :dependencies [[com.google.protobuf/protobuf-java "2.5.0"]
                 [potemkin "0.3.4"]
                 [midje "1.5.1" :scope "test"]])
