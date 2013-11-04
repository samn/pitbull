(ns protobuf
  (:import [com.google.protobuf
              Descriptors$FieldDescriptor
              Descriptors$FieldDescriptor$JavaType
              Message
              Message$Builder])
  (:require [potemkin :refer [def-map-type]]))

(def ^:private as-seq 
  "Function. Returns a flat seq with its argument as the contents."
  (comp flatten vector))

(defn- throw-invalid-field
  [message-or-builder field-name]
  (let [message-name (.. message-or-builder (getDescriptorForType) (getFullName))
        error (str "No Field named " field-name "found on Protocol Buffer Message of type " message-name)]
    (throw (IllegalArgumentException. error))))

(defn repeated-field?
  [^Descriptors$FieldDescriptor field-descriptor]
  (if (instance? Descriptors$FieldDescriptor field-descriptor)
    (.isRepeated field-descriptor)
    false))

(defn message-field?
  "Returns true if field-descriptor describes a message field.
  NOTE: repeated fields of a message will have the java type of message
  need to explicitly check if a field is repeated with `repeated-field?`."
  [^Descriptors$FieldDescriptor field-descriptor]
  (if (instance? Descriptors$FieldDescriptor field-descriptor)
    (= Descriptors$FieldDescriptor$JavaType/MESSAGE (.getJavaType field-descriptor))
    false))

;; TODO rename?
(defprotocol ProtobufMessageWrapper
  (get-message [_]))

(defn ^Message$Builder new-builder
  [^Class klass]
  (clojure.lang.Reflector/invokeStaticMethod klass "newBuilder" (to-array [])))

(defn find-field
  [^Message message s]
  (let [descriptor (.getDescriptorForType message)]
    (.findFieldByName descriptor (str s))))

(declare map->message)

(defn convert-value
  "Convert raw-value to the type expected for the given field."
  [^Message$Builder builder field raw-value]
  (if (message-field? field)
    (if (repeated-field? field)
      ;; TODO this always coerces the values for a repeated field into a seq, is that ok?
      (map #(map->message (.newBuilderForField builder field) %) (as-seq raw-value))
      (map->message (.newBuilderForField builder field) raw-value))
    raw-value))

(defn set-on-builder!
  [^Message$Builder builder ^Descriptors$FieldDescriptor field value]
  (let [converted-value (convert-value builder field value)]
    (if (repeated-field? field)
      (doseq [v (as-seq converted-value)]
        (.addRepeatedField builder field v))
      (.setField builder field converted-value))))

(defn map->message
  "Fills the values in Map m into Protobuf Builder builder.
  Returns a com.google.protobuf.Message."
  [^Message$Builder builder m]
  (let [descriptor (.getDescriptorForType builder)]
    (doseq [[k v] m]
      (let [field-name (name k)
            field (.findFieldByName descriptor field-name)]
        (if field
          (set-on-builder! builder field v)
          (throw-invalid-field builder field-name))))
    (.build builder)))

;; The ProtobufMap constructor shouldn't be used directly and is considered an implementation detail.
;; Use map->ProtobufMap to construct a ProtobufMap from a map with a specified protobuf definition.
;; Use message->ProtobufMap to construct a ProtobufMap that wraps a protobuf Message instance.
(def-map-type ProtobufMap [m meta-map]
  (get [_ k default-value]
    (let [field-name (name k)
          field (find-field m field-name)
          ;; getField returns the value, or a List of messages if a repeated field
          value (.getField m field)]
      (if value
        (cond
          (repeated-field? field) (map #(ProtobufMap. % meta-map) value)
          (message-field? field) (ProtobufMap. value meta-map)
          :else value)
        default-value)))
  (assoc [_ k v]
    (let [field-name (name k)
          field (find-field m field-name)]
      (if field
        (let [builder (.toBuilder m)]
          (set-on-builder! builder field v)
          (ProtobufMap. (.build builder) meta-map))
        (throw-invalid-field m field-name))))
  (dissoc [_ k]
    (let [field-name (name k)
          field (find-field m field-name)]
      (if field
        (let [builder (.toBuilder m)]
          (.clearField builder field)
          (ProtobufMap. (.build builder) meta-map))
        (throw-invalid-field m field-name))))
  (keys [_]
    (map #(.getName %) (.getFields (.getDescriptorForType m))))
  ProtobufMessageWrapper
  (get-message [_] m)
  clojure.lang.IObj
  (withMeta [this meta]
    (ProtobufMap. m meta))
  (meta [this]
    meta-map))

(defn message->ProtobufMap
  [^Message message]
  (ProtobufMap. message nil))

(defn map->ProtobufMap
  [message-class m]
  (let [builder (new-builder message-class)
        message (map->message builder m)]
    (ProtobufMap. (.build builder) nil)))

(defn load-protobuf
  [^Class klass ^java.io.InputStream input-stream]
  (let [args (to-array [input-stream])
        message (clojure.lang.Reflector/invokeStaticMethod klass "parseFrom" args)]
    (ProtobufMap. message nil)))

(extend-type Message
  ProtobufMessageWrapper
  (get-message [this] this))

(defn serialize-to
  [m output-stream]
  (let [protobuf-message (get-message m)]
    (.writeTo protobuf-message output-stream)))

(defn serialize-to-byte-stream
  "Serializes a com.google.protobuf.Message or ProtobufMap
  into a ByteArrayOutputStream."
  [m]
  (serialize-to m (java.io.ByteArrayOutputStream.)))
