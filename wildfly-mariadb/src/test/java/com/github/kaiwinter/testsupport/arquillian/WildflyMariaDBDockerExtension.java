package com.github.kaiwinter.testsupport.arquillian;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ProtocolDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.jdbc.ext.ScriptUtils;

import com.github.kaiwinter.testsupport.arquillian.WildflyArquillianRemoteConfiguration.ContainerConfiguration;
import com.github.kaiwinter.testsupport.arquillian.WildflyArquillianRemoteConfiguration.ContainerConfiguration.ServletProtocolDefinition;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

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

      private static final String DOCKER_IMAGE = "kaiwinter/wildfly10-mariadb:1.1";

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
      public void registerInstance(@Observes ContainerRegistry registry, ServiceLoader serviceLoader) {
         GenericContainer dockerContainer = new GenericContainer(DOCKER_IMAGE)
            .withExposedPorts(WILDFLY_HTTP_PORT, WILDFLY_MANAGEMENT_PORT, MARIADB_PORT);
         dockerContainer.start();

         configureArquillianForRemoteWildfly(dockerContainer, registry);

         setupDb(dockerContainer);
      }

      private void configureArquillianForRemoteWildfly(GenericContainer dockerContainer, ContainerRegistry registry) {
         Integer wildflyHttpPort = dockerContainer.getMappedPort(WILDFLY_HTTP_PORT);
         Integer wildflyManagementPort = dockerContainer.getMappedPort(WILDFLY_MANAGEMENT_PORT);

         String containerIpAddress = dockerContainer.getContainerIpAddress();
         Container arquillianContainer = registry.getContainers().iterator().next();
         ContainerDef containerConfiguration = arquillianContainer.getContainerConfiguration();
         containerConfiguration.property(ContainerConfiguration.MANAGEMENT_ADDRESS_KEY, containerIpAddress);
         containerConfiguration.property(ContainerConfiguration.MANAGEMENT_PORT_KEY,
            String.valueOf(wildflyManagementPort));
         containerConfiguration.property(ContainerConfiguration.USERNAME_KEY, "admin");
         containerConfiguration.property(ContainerConfiguration.PASSWORD_KEY, "Admin#007");

         ProtocolDef protocolConfiguration = arquillianContainer
            .getProtocolConfiguration(new ProtocolDescription(ServletProtocolDefinition.NAME));
         protocolConfiguration.property(ServletProtocolDefinition.HOST_KEY, containerIpAddress);
         protocolConfiguration.property(ServletProtocolDefinition.PORT_KEY, String.valueOf(wildflyHttpPort));
      }

      private void setupDb(GenericContainer dockerContainer) {
         String containerIpAddress = dockerContainer.getContainerIpAddress();
         Integer port3306 = dockerContainer.getMappedPort(MARIADB_PORT);
         String connectionString = "jdbc:mysql://" + containerIpAddress + ":" + port3306 + "/test";

         try (Connection connection = DriverManager.getConnection(connectionString, "admin", "admin");) {
            URL resource = Resources.getResource(DDL_FILE);
            String sql = Resources.toString(resource, Charsets.UTF_8);
            ScriptUtils.executeSqlScript(connection, "", sql);
         } catch (SQLException | ScriptException | IOException e) {
            throw new RuntimeException(e);
         }
      }
   }
}