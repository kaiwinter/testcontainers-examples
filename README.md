## testcontainers-examples

This repository demonstrates the use of [testcontainers](https://github.com/testcontainers/testcontainers-java) for self contained, reproducible integration tests using docker.
These tests can either be run (and debugged) from the IDE or by maven in an CI environment.


Currently there are these examples:

- [Testing a Hibernate-managed DB layer](https://github.com/kaiwinter/testcontainers-examples/tree/master/hibernate)
- [Testing a Java EE application in Wildfly 27 with DB access](https://github.com/kaiwinter/testcontainers-examples/tree/master/wildfly-mariadb)
  - [Second version without custom docker image](https://github.com/kaiwinter/testcontainers-examples/tree/master/wildfly-mariadb-plain-containers)
- [Testing a JSF UI using Docker](https://github.com/kaiwinter/testcontainers-examples/tree/master/jsf)