# Introduction to pitbull

Pitbull is a library for dealing with [Protocol Buffers](http://code.google.com/p/protobuf/) in Clojure. 
Regular Maps can be treated as Protobuf Messages and vice versa.
The structure of a Protobuf Message can be enforced eagerly (when manipulating a Map)
or lazily (at conversion/serialization time).

### ProtobufMap

ProtobufMap is the main type of Pitbull.
It wraps a Protobuf Message in an structure implementing IPersistentMap
and can be used like a regular Clojure immutable Map.
Protobuf Messages are immutable and so is ProtobufMap.

A ProtobufMap can be created from an existing Protobuf Message by using the `message->ProtobufMap` constructor.
Embedded Messages will be wrapped in a ProtobufMessage automatically when retrieved.
The values of repeated fields will be wrapped in a seq.

A ProtobufMap can be created from a Map by using the `map->ProtobufMap` constructor.
The class of the Protobuf Message the Map represents needs to be passed in too.
Each key of the Map should match the name of a field on the message definition.
An IllegalArgumentException will be thrown if key is present that isn't on the message definition.
An Exception will be thrown if a required field isn't present.

An instance of a Protobuf Message or a ProtobufMap can be serialized using `serialize-to` or `serialize-to-bytes`.
`serialize-to` will serialize a message/map to a given output-stream.
`serialize-to-bytes` will return a byte array of the serialized message/map.

An instance of ProtobufMap can be created from a serialized Protobuf Message using `load-protobuf`.

### Enforcing Structure

Pitbull lets you validate the structure of your data against a Protobuf definition.
By manipulating a ProtobufMap like a regular Map you can enforce constraints at the call site that breaks them.
`map->ProtobufMap` lets you take a Map (perhaps from a codebase that doesn't use Pitbull) and validate its structure.
Either approach lets you use the fast serialization/deserialization offered by Protobuf.
