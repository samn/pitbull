(ns pitbull
  "Wrap Protocol Buffers Messages as Maps.
  Convert Maps to Protocol Buffer Messages."
  (:import [com.google.protobuf
              Descriptors$FieldDescriptor
              Descriptors$FieldDescriptor$JavaType
              Message
              Message$Builder
              MessageOrBuilder])
  (:require [potemkin :refer [def-map-type]]))

;;;; Private Functions

(def ^{:private true} as-seq 
  "Function. Returns a flat seq with its argument as the contents."
  (comp flatten vector))

(defn- throw-invalid-field
  [^MessageOrBuilder message-or-builder field-name]
  (let [message-name (.. message-or-builder (getDescriptorForType) (getFullName))
        error (str "No Field named " field-name " found on Protocol Buffer Message of type " message-name)]
    (throw (IllegalArgumentException. error))))

(defn- ^{:testable true} repeated-field?
  [^Descriptors$FieldDescriptor field-descriptor]
  (and (instance? Descriptors$FieldDescriptor field-descriptor)
       (.isRepeated field-descriptor)))

(defn- ^{:testable true} message-field?
  "Returns true if field-descriptor describes a message field.
  NOTE: repeated fields of a message will have the java type of message
  need to explicitly check if a field is repeated with `repeated-field?`."
  [^Descriptors$FieldDescriptor field-descriptor]
  (and (instance? Descriptors$FieldDescriptor field-descriptor)
       (= Descriptors$FieldDescriptor$JavaType/MESSAGE (.getJavaType field-descriptor))))

(defprotocol ProtobufMessageWrapper
  (get-message [_]))

(extend-type Message
  ProtobufMessageWrapper
  (get-message [this] this))

(defn- ^Message$Builder new-builder
  "Instantiates & returns a new Message$Builder for the Class klass."
  [^Class klass]
  (clojure.lang.Reflector/invokeStaticMethod klass "newBuilder" (to-array [])))

(defn- ^Descriptors$FieldDescriptor find-field
  "Find and returns the FieldDescriptor for field-name on Message message.
  Returns nil if no field with that name exists."
  [^Message message field-name]
  (let [descriptor (.getDescriptorForType message)
        field (.findFieldByName descriptor field-name)]
    (if (nil? field)
      (throw-invalid-field message field-name)
      field)))

(declare map->message)

(defn- conversion-fn
  "Returns a function that converts a raw value to the appropriate type
  for the given field.  Returns identity if no conversion is needed."
  [^Message$Builder builder ^Descriptors$FieldDescriptor field]
  (cond
    (message-field? field) #(map->message (.newBuilderForField builder field) %)
    ;; Clojure treats numbers as Longs by default, so an explicit cast to Integer is needed
    (= (.getJavaType field) Descriptors$FieldDescriptor$JavaType/INT) int
    :else identity))

(defn- convert-value
  "Convert raw-value to the type expected for the given field.
  If the field message field:
    raw-value is expected to be a Map and will be converted to a Message.
    the keys of the raw-value map will be used to find Fields on the Message.
  If the field is an Int then raw-value is cast to an Integer
  If the field is repeated:
    a seq of raw-value (or the values in it if raw-value is iterable) will be returned.
    any above transformations will be applied to each repeated value.
  Otherwise no transformation is done and raw-value is returned."
  [^Message$Builder builder ^Descriptors$FieldDescriptor field raw-value]
  (let [conversion-fn (conversion-fn builder field)]
    (if (repeated-field? field)
      (map conversion-fn raw-value)
      (conversion-fn raw-value))))

(defn- set-on-builder!
  "Sets the value for FieldDescriptor field on Message.Builder builder to be value.
  Converts the value as needed first.  See convert-value."
  [^Message$Builder builder ^Descriptors$FieldDescriptor field value]
  (let [converted-value (convert-value builder field value)]
    (if (repeated-field? field)
      (doseq [v (as-seq converted-value)]
        (.addRepeatedField builder field v))
      (.setField builder field converted-value))))

(defn- ^Message map->message
  "Fills the values in Map m into Message.Builder builder.
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

;;;; Data Types

;;;; The ProtobufMap constructor shouldn't be used directly and is considered an implementation detail.
;;;; Use map->ProtobufMap to construct a ProtobufMap from a map with a specified protobuf definition.
;;;; Use message->ProtobufMap to construct a ProtobufMap that wraps a protobuf Message instance.
(def-map-type ProtobufMap [^Message m meta-map]
  (get [_ k default-value]
    (let [field-name (name k)
          field (find-field m field-name)
          ;; getField returns the value, or a List of messages if a repeated field
          value (.getField m field)]
      (if value
        (cond
          (and (repeated-field? field) (message-field? field)) (map #(ProtobufMap. % meta-map) value)
          (message-field? field) (ProtobufMap. value meta-map)
          :else value)
        default-value)))
  (assoc [_ k v]
    (let [field-name (name k)
          field (find-field m field-name)
          builder (.toBuilder m)]
      (set-on-builder! builder field v)
      (ProtobufMap. (.build builder) meta-map)))
  (dissoc [_ k]
    (let [field-name (name k)
          field (find-field m field-name)
          builder (.toBuilder m)]
      (.clearField builder field)
      (ProtobufMap. (.build builder) meta-map)))
  (keys [_]
    (map #(.getName ^Descriptors$FieldDescriptor %) (.getFields (.getDescriptorForType m))))
  ProtobufMessageWrapper
  (get-message [_] m)
  clojure.lang.IObj
  (withMeta [this meta]
    (ProtobufMap. m meta))
  (meta [this]
    meta-map))

;;;; Public Interface

(defn message->ProtobufMap
  "Return a ProtobufMap that wraps the Protobuf Message message."
  [^Message message]
  (ProtobufMap. message nil))

(defn map->ProtobufMap
  "Returns a ProtobufMap with the values of Map m validated against the
  protobuf Message class message-class. If there are keys on m that are not
  valid field names for message-class then an IllegalArgument exception will be thrown.
  Nested messages are supported (as nested Maps), repeated fields as seqs."
  [message-class m]
  (let [builder (new-builder message-class)
        message (map->message builder m)]
    (ProtobufMap. (.build builder) nil)))

(defn load-protobuf
  "Reads a serialized protobuf Message from input-stream and deserializes it
  using the class definition klass.  Wraps the deserialized message in a ProtobufMap."
  [^Class klass ^java.io.InputStream input-stream]
  (let [args (to-array [input-stream])
        message (clojure.lang.Reflector/invokeStaticMethod klass "parseFrom" args)]
    (ProtobufMap. message nil)))

(defn bytes->ProtobufMap
  "Deserialize a byte array containing a serialized Protobuf Message of type
  klass and wrap in a ProtobufMap."
  [^Class klass protobuf-bytes]
  (let [input-stream (java.io.ByteArrayInputStream. protobuf-bytes)]
    (load-protobuf klass input-stream)))

(defn serialize-to
  "Serialize Message or ProtobufMap m and write it to OutputStream output-stream."
  [m ^java.io.OutputStream output-stream]
  (let [^Message protobuf-message (get-message m)]
    (.writeTo protobuf-message output-stream)))

(defn serialize-to-bytes
  "Serializes a com.google.protobuf.Message or ProtobufMap into a byte array"
  [m]
  (let [output-stream (java.io.ByteArrayOutputStream.)]
    (serialize-to m output-stream)
    (.toByteArray output-stream)))

(defn serialize-map
  "Serialize Map m as a Protobuf of type klass to OutputStream output-stream."
  [^Class klass m ^java.io.OutputStream output-stream]
  (let [protobuf-map (map->ProtobufMap klass m)]
    (serialize-to protobuf-map output-stream)))

(defn map->proto-bytes
  "Serialize Map m as a Protobuf of Class klass."
  [^Class klass m]
  (let [protobuf-map (map->ProtobufMap klass m)]
    (serialize-to-bytes protobuf-map)))
