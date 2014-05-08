akka-gehversuch
===============

First attempts to become "reactive" ;-)

About this project:
I tried to understand how Akka with Scala feels like. To get started an easy SOAP webservice came into my mind. At the
moment this project concentrates solely on consuming/transforming/routing of a SOAP message and the way back to its
producer.

Build:
mvn clean install
(generate Java code from WSDL, compile Java/Scala sources, archive)

Run:
mvn exec:java
(starts the consumer)

Test:
* open SOAP UI
* generate a project out of the WSDL file
* submit SOAP request

===============
TODO:
* still a problem that the namespaces inside of the SOAP message will not be validated
* getting rid of camel extension completely (maybe choose another framework like Spray or Play)
* add unit & integration tests
* dive into service implementation
