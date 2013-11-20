# pitbull

Clojure Protocol Buffers



### [Pitbull](https://en.wikipedia.org/wiki/Pitbull_%28rapper%29) 
is a rapper.

###[pitbull](https://en.wikipedia.org/wiki/Pitbull)
is a wrapper around Protocol Buffer objects that exposes an `IPersistentMap`-like interface.

## Installation

pitbull is available on [Clojars](https://clojars.org/pitbull).


	[pitbull "0.1.0"]


## Usage

*See [the docs](https://github.com/samn/pitbull/blob/master/doc/intro.md)*

Given a Protocol Buffer Message:

	package Pets;
	option java_package = "com.pets";

	message Bear {
    	required string name = 1;
	    optional bool hungry = 2;
	}
	
that has been used to generate a Java class (`com.pets.Pets$Bear`).

	(require '[pitbull :as pb])
	(import '[com.pets Pets$Bear])

You can create a new wrapped `Pets$Bear` instance from a regular `Map`:
	
	(def bear-map {:name "yogi" :hungry true})
	(def bear-protobuf-map (pb/map->ProtobufMap Pets$Bear bear-map))
	
	(:name bear-protobuf-map) ; => "yogi"
	;; fields can be accessed using String or Keyword keys
	(get bear-protobuf-map "hungry") ; => true
	
A `ProtobufMap` will throw an `IllegalArgumentException` if a key that doesn't exist on the Protobuf definition is added:

	(assoc bear-protobuf-map :color "brown") ; => IllegalArgumentException

An Exception will be thrown if you try to create a `ProtobufMap` with an invalid key, or if a required field is missing:

	(pb/map->ProtobufMap Pets$Bear {:hungry false}) ; => com.google.protobuf.UninitializedMessageException
	(pb/map->ProtobufMap Pets$Bear {:name "boo boo" :size "small"}) ; => IllegalArgumentException
	
You can serialize a `ProtobufMap` to a given `OutputStream`
	
	(pb/serialize-to bear-protobuf-map (java.io.FileOutputStream "yogi.bear"))
	
A convenience function is provided to serialize directly to a byte array:

	(pb/serialize-to-bytes bear-protobuf-map)
	
Or even a Map directly to a byte-array (or vice versa):

	(->> {:name "Misha"}
		 (pb/map->proto-bytes Pets$Bear)
		 (bytes->ProtobufMap Pets$Bear))

## Build Status

[![Travis CI](https://api.travis-ci.org/samn/pitbull.png)](https://travis-ci.org/samn/pitbull)


## [Pitbulls](https://en.wikipedia.org/wiki/Pitbull) 

are the best kind of dog.  If you could be a good guardian you should consider adopting one.
![so cute!](https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/American_Pit_Bull_Terrier_-_Seated.jpg/443px-American_Pit_Bull_Terrier_-_Seated.jpg)

## License

Copyright © 2013 @samn

Distributed under the Eclipse Public License, the same as Clojure.
