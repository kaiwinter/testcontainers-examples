package com.github.kaiwinter.testsupport.arquillian;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

import javax.script.ScriptException;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ProtocolDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.as.arquillian.container.Authentication;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.jdbc.ContainerLessJdbcDelegate;
import org.testcontainers.shaded.com.google.common.base.Charsets;
import org.testcontainers.shaded.com.google.common.io.Resources;
import org.testcontainers.utility.DockerImageName;
import org.wildfly.plugin.core.Deployment;
import org.wildfly.plugin.core.DeploymentManager;

import com.github.kaiwinter.testsupport.arquillian.WildflyArquillianRemoteConfiguration.ContainerConfiguration;
import com.github.kaiwinter.testsupport.arquillian.WildflyArquillianRemoteConfiguration.ContainerConfiguration.ServletProtocolDefinition;

/**
 * Starts a docker container and configures Arquillian to use Wildfly in the docker container.
 */
public final class WildflyMariaDBDockerExtension implements LoadableExtension {

   @Override
   public void register(ExtensionBuilder builder) {
      builder.observer(LoadContainerConfiguration.class);
   }

   /**
    * Helper class to register an Arquillian observer.
    */
   public static final class LoadContainerConfiguration {

      private static final String MARIA_DB_NETWORK_HOSTNAME = "mariadbcontainer";
      private static final String WILDFLY_PWD = "Admin#007";
      private static final String WILDFLY_USER = "admin";
      private static final String MARIADB_DOCKER_IMAGE = "library/mariadb:10.10.2";
      private static final String WILDFLY_DOCKER_IMAGE = "quay.io/wildfly/wildfly:27.0.0.Final-jdk17";

      private static final int WILDFLY_HTTP_PORT = 8080;
      private static final int WILDFLY_MANAGEMENT_PORT = 9990;
      private static final int MARIADB_PORT = 3306;

      private static final String DDL_FILE = "DDL.sql";

      /**
       * Method which observes {@link ContainerRegistry}. Gets called by Arquillian at startup time.
       * 
       * @param registry
       *           contains containers defined in arquillian.xml
       * @param serviceLoader
       */
      public void registerInstance(@Observes ContainerRegistry registry, ServiceLoader serviceLoader) throws IOException {
         Network mariaDBAppserverNetwork = Network.newNetwork();
         DockerImageName mariaDBImageName = DockerImageName.parse(MARIADB_DOCKER_IMAGE)
               .asCompatibleSubstituteFor("mariadb");
         @SuppressWarnings("resource")
         MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>(mariaDBImageName)
               .withNetwork(mariaDBAppserverNetwork)
               .withNetworkAliases(MARIA_DB_NETWORK_HOSTNAME)
               .withExposedPorts(MARIADB_PORT)
               .withDatabaseName("test")
               .withUsername("admin")
               .withPassword("admin");
         mariaDBContainer.start();
         @SuppressWarnings("resource")
         GenericContainer<?> wildflyContainer = new GenericContainer<>(
               new ImageFromDockerfile()
                     .withDockerfileFromBuilder(builder -> builder.from(WILDFLY_DOCKER_IMAGE)
                           .user("jboss")
                           .run("/opt/jboss/wildfly/bin/add-user.sh " + WILDFLY_USER + " " + WILDFLY_PWD + " --silent")
                           .cmd("/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0")
                           .build()))
               .withNetwork(mariaDBAppserverNetwork)
               .withExposedPorts(WILDFLY_MANAGEMENT_PORT, WILDFLY_HTTP_PORT)
               .withStartupTimeout(Duration.ofSeconds(30));
         wildflyContainer.start();
         addDatasourceToWildflyContainer(wildflyContainer);

         configureArquillianForRemoteWildfly(wildflyContainer, registry);

         setupDb(mariaDBContainer);
      }

      private void addDatasourceToWildflyContainer(GenericContainer<?> wildflyContainer) throws IOException {
         Authentication.username = WILDFLY_USER;
         Authentication.password = WILDFLY_PWD;

         ModelControllerClientConfiguration clientConfig = new ModelControllerClientConfiguration.Builder()
               .setHostName(wildflyContainer.getHost())
               .setPort(wildflyContainer.getMappedPort(WILDFLY_MANAGEMENT_PORT))
               .setHandler(Authentication.getCallbackHandler())
               .build();

         File driverFile = Maven.resolver()
               .loadPomFromFile("pom.xml")
               .resolve("org.mariadb.jdbc:mariadb-java-client")
               .withoutTransitivity()
               .asSingleFile();

         ModelControllerClient client = ModelControllerClient.Factory.create(clientConfig);
         DeploymentManager deploymentManager = DeploymentManager.Factory.create(client);
         deploymentManager.forceDeploy(Deployment.of(new FileInputStream(driverFile), "mariadb.jar"));

         ModelNode request = new ModelNode();
         request.get(ClientConstants.OP).set(ClientConstants.ADD);
         request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
         request.get(ClientConstants.OP_ADDR).add("data-source", "java:/MyApplicationDS");
         request.get("jndi-name").set("java:/MyApplicationDS");
         request.get("connection-url").set(
               "jdbc:mariadb://" + MARIA_DB_NETWORK_HOSTNAME + "/test?autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8");
         request.get("driver-class").set("org.mariadb.jdbc.Driver");
         request.get("driver-name").set("mariadb.jar");
         request.get("user-name").set("admin");
         request.get("password").set("admin");
         request.get("pool-name").set("pool_MyApplicationDS");
         client.execute(new OperationBuilder(request).build());
      }

      private void configureArquillianForRemoteWildfly(GenericContainer<?> paramWildflyContainer,
            ContainerRegistry registry) {
         Integer wildflyHttpPort = paramWildflyContainer.getMappedPort(WILDFLY_HTTP_PORT);
         Integer wildflyManagementPort = paramWildflyContainer.getMappedPort(WILDFLY_MANAGEMENT_PORT);

         String containerIpAddress = paramWildflyContainer.getHost();
         Container arquillianContainer = registry.getContainers().iterator().next();
         ContainerDef containerConfiguration = arquillianContainer.getContainerConfiguration();
         containerConfiguration.property(ContainerConfiguration.MANAGEMENT_ADDRESS_KEY, containerIpAddress);
         containerConfiguration.property(ContainerConfiguration.MANAGEMENT_PORT_KEY,
               String.valueOf(wildflyManagementPort));
         containerConfiguration.property(ContainerConfiguration.USERNAME_KEY, WILDFLY_USER);
         containerConfiguration.property(ContainerConfiguration.PASSWORD_KEY, WILDFLY_PWD);

         ProtocolDef protocolConfiguration = arquillianContainer
               .getProtocolConfiguration(new ProtocolDescription(ServletProtocolDefinition.NAME));
         protocolConfiguration.property(ServletProtocolDefinition.HOST_KEY, containerIpAddress);
         protocolConfiguration.property(ServletProtocolDefinition.PORT_KEY, String.valueOf(wildflyHttpPort));
      }

      private void setupDb(MariaDBContainer<?> dockerContainer) {
         String containerIpAddress = dockerContainer.getHost();
         Integer mappedDatabasePort = dockerContainer.getMappedPort(MARIADB_PORT);
         String connectionString = "jdbc:mariadb://" + containerIpAddress + ":" + mappedDatabasePort + "/test";

         try (Connection connection = DriverManager.getConnection(connectionString, "admin", "admin");) {
            URL resource = Resources.getResource(DDL_FILE);
            String sql = Resources.toString(resource, Charsets.UTF_8);
            ScriptUtils.executeDatabaseScript(new ContainerLessJdbcDelegate(connection), "", sql);
         } catch (SQLException | ScriptException | IOException e) {
            throw new RuntimeException(e);
         }

      }
   }
}