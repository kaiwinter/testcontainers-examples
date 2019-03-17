## testcontainers-examples: jsf

For these tests a docker image is used which contains a Wildfly 14: [kaiwinter/wildfly14-mgmt-user](https://hub.docker.com/r/kaiwinter/wildfly14-mgmt-user).
A management user is set-up to let Arquillian deploy to this server.
For the Arquillian deployment `wildfly-arquillian-container-remote` is used. 

The tricky part is the dynamic configuration of Arquillian to deploy the application to Wildfly.
This is necessary because testcontainers exposes the real application server port at a random port to support the parallel use of multiple containers.
Arquillian is configured by the file `arquillian.xml` and it cannot be changed by an API dynamically [[GitHub Issue](https://github.com/wildfly/wildfly-arquillian/issues/72)].
But there is the possibility to register a `org.jboss.arquillian.core.spi.LoadableExtension` which registers a listener on the configuration process (see [WildflyMariaDBDockerExtension](https://github.com/kaiwinter/testcontainers-examples/blob/master/jsf/src/test/java/com/github/kaiwinter/testsupport/arquillian/WildflyDockerExtension.java)).
Arquillian can then be completely configured by the listener class and the [`arquillian.xml`](https://github.com/kaiwinter/testcontainers-examples/blob/master/jsf/src/test/resources/arquillian.xml) is almost empty:
```xml
<arquillian xmlns="http://jboss.org/schema/arquillian" 
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://jboss.org/schema/arquillian">
   <container qualifier="wildfly-docker" default="true">
      <protocol type="Servlet 3.0" />
   </container>
</arquillian>
```
The listener class ([WildflyDockerExtension](https://github.com/kaiwinter/testcontainers-examples/blob/master/jsf/src/test/java/com/github/kaiwinter/testsupport/arquillian/WildflyDockerExtension.java)) starts the docker container by the library [testcontainers](https://github.com/testcontainers/testcontainers-java) and configures Arquillian. Then the normal Arquillian test runs which means `wildfly-arquillian-container-remote` deploys the war file to the server and runs the `@Test`-methods.

This is how the test class looks like. There is no difference from a normal Arquillian test. 

```java
@RunWith(Arquillian.class)
@RunAsClient
public final class UserViewTest {

   @Drone
   private WebDriver driver;

   @Deployment
   static WebArchive createDeployment() {
      WebArchive war = ShrinkWrap.create(WebArchive.class) //
            .addClasses(UserView.class, UserService.class, User.class) //
            .addAsWebResource(new File("src/main/webapp/users.xhtml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml")) //
            .addAsLibraries(Maven.resolver() //
                  .loadPomFromFile("pom.xml") //
                  .importCompileAndRuntimeDependencies() //
                  .resolve().withoutTransitivity().asFile());

      return war;
   }

   /**
    * Tests if the xhtml page contains a table with five rows.
    */
   @Test
   public void tableContainsData() {
      String address = WildflyDockerExtension.baseUrl + "users.xhtml";
      driver.get(address);
      WebElement datatable = driver.findElement(By.className("ui-datatable-data"));

      List<WebElement> datatableRows = datatable.findElements(By.className("ui-widget-content"));
      assertEquals(5, datatableRows.size());
   }
}
```