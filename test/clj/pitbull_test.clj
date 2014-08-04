(set! *warn-on-reflection* true)
(ns pitbull-test
  (:import [com.samn Test$Foo])
  (:require [pitbull :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables pitbull)

(def foo-descriptor (Test$Foo/getDescriptor))
(def bar-descriptor (.getMessageType (.findFieldByName foo-descriptor "bar")))

(tabular
  (fact "message-field?"
    (message-field? ?descriptor) => ?message?)
  ?descriptor  ?message?
  (.findFieldByName foo-descriptor "bar")  true
  (.findFieldByName bar-descriptor "bazs")  true
  (.findFieldByName foo-descriptor "sattr")  false
  1  false)

(tabular
  (fact "repeated-field?"
    (repeated-field? ?descriptor) => ?message?)
  ?descriptor  ?message?
  (.findFieldByName foo-descriptor "bar")  false
  (.findFieldByName bar-descriptor "bazs")  true
  (.findFieldByName foo-descriptor "sattr")  false
  (.findFieldByName foo-descriptor "iiattr")  true
  1  false)

(let [m (load-protobuf Test$Foo (java.io.FileInputStream. "test/resources/serialized/test1"))] 
  (fact "load-protobuf returns a ProtobufMap"
    (class m) => pitbull.ProtobufMap)
  (fact "assoc"
    (get (assoc m "iattr" 2) "iattr")  => 2)
  (fact "dissoc"
    (get (dissoc m "iattr") "iattr")  => 0)
  (fact "nested messages"
    (get-in (assoc m "bar" {"bazs" [{"baz" "hi"}]}) ["bar" "bazs"]) => [{"baz" "hi"}])
  (fact "keywords or strings can be used as keys"
    (:iattr m) => 1
    (:sattr (assoc m :sattr "dogs")) => "dogs"
    (:iattr (dissoc m :iattr)) => 0)
  (fact "get, assoc, & dissoc throw an exception for keys that aren't present on the protobuf"
    (get m "invalid") => (throws IllegalArgumentException)
    (assoc m "invalid" 1) => (throws IllegalArgumentException)
    (dissoc m "invalid") => (throws IllegalArgumentException)))

(let [m {"iattr" 350
         "iiattr" [150 250 350]
         "sattr" "lock ness"
         "bar" {"bazs" [{"baz" "hi"} {"baz" "ho"}]}}
      p-m (map->ProtobufMap Test$Foo m)]
  (fact "map->ProtobufMap creates a ProtobufMap"
    (get p-m "iattr") => (get m "iattr")
    (get p-m "iiattr") => (get m "iiattr")
    (get p-m "sattr") => (get m "sattr")
    (get-in p-m ["bar" "bazs"]) => (get-in m ["bar" "bazs"])))

(let [m {:sattr "s" "iattr" 1 :bar {:bazs [{:baz "hi"}]}}]
  (fact "map->ProtobufMap works with mixed keys"
    (map->ProtobufMap Test$Foo m) =not=> (throws Exception)))

(let [p-m (map->ProtobufMap Test$Foo {:sattr "s"})]
  (fact "map->ProtobufMap throws an exception on invalid fields"
    (dissoc p-m "dogs") => (throws IllegalArgumentException)
    (assoc p-m "dogs" "yes") => (throws IllegalArgumentException)
    (map->ProtobufMap Test$Foo {"dogs" "yes"}) => (throws IllegalArgumentException)))

(let [m {:sattr "s" :iattr 700}]
  (fact "serialize -> deserialize"
    (->> m (map->proto-bytes Test$Foo) (bytes->ProtobufMap Test$Foo)) => (map->ProtobufMap Test$Foo m)))

