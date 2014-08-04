(set! *warn-on-reflection* true)
(ns perf
  "Basic performance test for pitbul.
  Run like this: lein with-profile perf run -m perf"
  (:require [pitbull :refer :all]
            [criterium.core :refer :all]
            [clojure.java.io :as io])
  (:import [java.io ByteArrayInputStream])
  (:gen-class))

(defn read-bytes
  [file-path]
  (let [file (io/file file-path)
        buffer (byte-array (.length file))
        stream (io/input-stream file)]
    (.read stream buffer)
    buffer))

(defn log-section
  [message]
  (println)
  (println)
  (println message))

(defn bench-raw-map
  []
  (log-section "Benchmarking raw Map access")
  (bench
    (let [raw-map {:iattr 350
                   :sattr "lock ness"
                   :bar {:bazs [{:baz "hi"} {:baz "ho"}]}}]
      (-> raw-map :bar :bazs first :baz))))

(defn bench-pbuf-map
  []
  (log-section "Benchmarking ProtobufMap construction & access")
  (bench
    (let [pb-map (map->ProtobufMap com.samn.Test$Foo {:iattr 350
                                                      :sattr "lock ness"
                                                      :bar {:bazs [{:baz "hi"} {:baz "ho"}]}})]
      (-> pb-map :bar :bazs first :baz))))

(defn bench-pbuf-map-access
  []
  (log-section "Benchmarking ProtobufMap access")
  (let [pb-map (map->ProtobufMap com.samn.Test$Foo {:iattr 350
                                                    :sattr "lock ness"
                                                    :bar {:bazs [{:baz "hi"} {:baz "ho"}]}})]
    (bench
      (-> pb-map :bar :bazs first :baz))))

;; TODO bench raw protobuf operations similar to above

(defn bench-load-pbufmap
  []
  (log-section "Benchmarking loading a protobuf from disk with load-protobuf")
  (let [proto-bytes (read-bytes "test/resources/serialized/test1")]
    (bench
      (let [pb-map (bytes->ProtobufMap com.samn.Test$Foo proto-bytes)]
        (-> pb-map :bar :bazs first :baz)))))

(defn bench-load-pbuf
  []
  (log-section "Benchmarking loading a protobuf from disk with raw protobuf")
  (let [proto-bytes (read-bytes "test/resources/serialized/test1")]
    (bench
      (let [pbuf (com.samn.Test$Foo/parseFrom (ByteArrayInputStream. proto-bytes))]
        (.. pbuf (getBar) (getBazsList) (get 0) (getBaz))))))

(defn -main
  [& args]
  (bench-raw-map)
  (bench-pbuf-map)
  (bench-pbuf-map-access)
  (bench-load-pbufmap)
  (bench-load-pbuf))
