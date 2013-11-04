(ns protobuf-test
  (:require [protobuf :refer :all]
            [midje.sweet :refer :all]))

(def foo-descriptor (com.samn.Test$Foo/getDescriptor))
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
  1  false)

(let [m (load-protobuf com.samn.Test$Foo (java.io.FileInputStream. "test/resources/serialized/test1"))] 
  (fact "load-protobuf returns a ProtobufMap"
    (class m) => protobuf.ProtobufMap)
  (fact "assoc"
    (get (assoc m "iattr" 2) "iattr")  => 2)
  (fact "dissoc"
    (get (dissoc m "iattr") "iattr")  => 0)
  (fact "nested messages"
    (get-in (assoc m "bar" {"bazs" [{"baz" "hi"}]}) ["bar" "bazs"]) => [{"baz" "hi"}])
  (fact "keywords or strings can be used as keys"
    (:iattr m) => 1
    (:sattr (assoc m :sattr "dogs")) => "dogs"
    (:iattr (dissoc m :iattr)) => 0))

(let [m {"iattr" 350
          "sattr" "lock ness"
          "bar" {"bazs" [{"baz" "hi"} {"baz" "ho"}]}}
      p-m (map->ProtobufMap com.samn.Test$Foo m)]
  (fact "map->ProtobufMap creates a ProtobufMap"
    (get p-m "iattr") => (get m "iattr")
    (get p-m "sattr") => (get m "sattr")
    (-> p-m (get "bar") (get "bazs") first) => (-> m (get "bar") (get "bazs") first)
        ))
    ;(get-in p-m ["bar" "bazs"]) => (get-in m ["bar" "bazs"])))

(let [p-m (map->ProtobufMap com.samn.Test$Foo {})]
  (fact "map->ProtobufMap throws an exception on invalid fields"
    (dissoc p-m "dogs") => (throws IllegalArgumentException)
    (assoc p-m "dogs" "yes") => (throws IllegalArgumentException)
    (map->ProtobufMap com.samn.Test$Foo {"dogs" "yes"}) => (throws IllegalArgumentException)))
