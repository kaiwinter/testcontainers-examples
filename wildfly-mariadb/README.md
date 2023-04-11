## testcontainers-examples: wildfly-mariadb

For these tests a docker image is used which contains a Wildfly 27 and a MariaDB installation: [kaiwinter/wildfly27-mariadb](https://github.com/kaiwinter/wildfly27-mariadb/pkgs/container/wildfly27-mariadb).
Wildfly is configured to use the MariaDB and a management user is set-up to let Arquillian deploy to this server.
For the Arquillian deployment `wildfly-arquillian-container-remote` is used. 

The hard part is the dynamic configuration of Arquillian to deploy the application to Wildfly.
This is necessary because testcontainers maps the real application server and database server ports to random ports to support the parallel use of multiple containers.
Arquillian is configured by the file `arquillian.xml` and it cannot be changed by an API dynamically.
But there is the possibility to register a `org.jboss.arquillian.core.spi.LoadableExtension` which registers a listener on the configuration process (see [WildflyMariaDBDockerExtension](https://github.com/kaiwinter/testcontainers-examples/blob/master/wildfly-mariadb/src/test/java/com/github/kaiwinter/testsupport/arquillian/WildflyMariaDBDockerExtension.java)).
Arquillian can then be completely configured by the listener class and the [`arquillian.xml`](https://github.com/kaiwinter/testcontainers-examples/blob/master/wildfly-mariadb/src/test/resources/arquillian.xml) is almost empty:
```xml
<arquillian xmlns="http://jboss.org/schema/arquillian" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://jboss.org/schema/arquillian">
   <container qualifier="wildfly-mariadb-docker" default="true">
      <protocol type="Servlet 5.0" />
   </container>
</arquillian>
```
The listener class ([WildflyMariaDBDockerExtension](https://github.com/kaiwinter/testcontainers-examples/blob/master/wildfly-mariadb/src/test/java/com/github/kaiwinter/testsupport/arquillian/WildflyMariaDBDockerExtension.java)) starts the docker container by the library [testcontainers](https://github.com/testcontainers/testcontainers-java), configures Arquillian, and inserts the DB model using a JDBC connection. Then the normal Arquillian test runs which means `wildfly-arquillian-container-remote` deploys the war file to the server and runs the `@Test`-methods.

This is how the test class looks like. There is no difference from a normal Arquillian test. The unit test inserts it's test data by [DBUnit](http://dbunit.sourceforge.net) which empties the database before the data is inserted. That way each unit test can insert different data.
```java
@ExtendWith(ArquillianExtension.class)
class UserServiceTest {

   @Inject
   private UserService userService;

   @PersistenceContext
   private EntityManager entityManager;

   @Deployment
   public static EnterpriseArchive createDeployment() {
      WebArchive war = ShrinkWrap.create(WebArchive.class) //
         .addClasses(UserService.class, UserRepository.class, User.class) //
         .addClasses(UserServiceTest.class, DockerDatabaseTestUtil.class) //
         .addAsResource("META-INF/persistence.xml") //
         .addAsResource("testdata.xml") //
         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      File[] pomDependencies = Maven.resolver() //
         .loadPomFromFile("pom.xml").importDependencies(ScopeType.TEST) //
         .resolve().withTransitivity().asFile();

      EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class) //
         .addAsModule(war) //
         .addAsLibraries(pomDependencies);

      return ear;
   }

   @Test
   void testSumOfLogins() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));
      int sumOfLogins = userService.calculateSumOfLogins();
      assertEquals(9, sumOfLogins);
   }
}
```