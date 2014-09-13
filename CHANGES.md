# pitbull

### Next Release

## Version 0.1.3

* Clojure 1.6 support

### Version 0.1.2

* Added a simple performance test comparing ProtobufMap against a Map literal & Protocol Buffers Message.
* Fixed a problem that prevented repeated fields of primitive (non-Message) values from working.
* Performance improvements (type hints reduced the amount of reflection).

### Version 0.1.1

* get, assoc, and dissoc on ProtobufMap throw an IllegalArgumentException for 
  keys that don't exist on the wrapped Message.

### Version 0.1.0

* Initial Release
