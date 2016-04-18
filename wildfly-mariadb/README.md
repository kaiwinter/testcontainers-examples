## testcontainers-examples: wildfly-mariadb

For these tests a prepared docker image is used which contains a Wildfly 10 and a MariaDB installation: [kaiwinter/wildfly10-mariadb](https://hub.docker.com/r/kaiwinter/wildfly10-mariadb/).
Wildfly is configured to use the MariaDB and a management user is set-up to let Arquillian deploy to this server.
For the Arquillian deployment wildfly-arquillian-container-remote is used. 

The hard part is the dynamic configuration of Arquillian to deploy to the Wildfly.
Arquillian is configured by the file `arquillian.xml` and it cannot be changed by an API dynamically.
But there is the possibility to register a `org.jboss.arquillian.core.spi.LoadableExtension` service which can register a listener on the configuration process ([WildflyMariaDBDockerExtension](https://github.com/kaiwinter/testcontainers-examples/blob/master/wildfly-mariadb/src/test/java/com/github/kaiwinter/testsupport/arquillian/WildflyMariaDBDockerExtension.java)).
Arquillian can then be completely configured by the listener class and the [`arquillian.xml`](https://github.com/kaiwinter/testcontainers-examples/blob/master/wildfly-mariadb/src/test/resources/arquillian.xml) is almost empty:
```java
<arquillian xmlns="http://jboss.org/schema/arquillian" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://jboss.org/schema/arquillian">
   <container qualifier="wildfly-mariadb-docker" default="true">
      <protocol type="Servlet 3.0" />
   </container>
</arquillian>
```

The listener class starts the docker container by the library [testcontainers](https://github.com/testcontainers/testcontainers-java) and inserts the DB model by using [Flyway](http://flywaydb.org/). Then the Arquillian remote extension deploys the war file to the server and runs the tests.