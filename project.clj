(defproject protobuf "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :test-paths ["test/clj"]
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test/java"]
  :uberjar-exclusions [#"test/java"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.google.protobuf/protobuf-java "2.5.0"]
                 [potemkin "0.3.4"]
                 [midje "1.5.1" :scope "test"]])
