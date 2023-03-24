package com.github.kaiwinter.testsupport.arquillian;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.testcontainers.containers.GenericContainer;

import com.github.kaiwinter.testsupport.arquillian.WildflyArquillianRemoteConfiguration.ContainerConfiguration;

/**
 * Starts a docker container and configures Arquillian to use Wildfly in the docker container.
 */
public final class WildflyDockerExtension implements LoadableExtension {

   /** The base URL of Wildfly in the Docker container. */
   public static String baseUrl;

   @Override
   public void register(ExtensionBuilder builder) {
      builder.observer(LoadContainerConfiguration.class);
   }

   /**
    * Helper class to register an Arquillian observer.
    */
   public static final class LoadContainerConfiguration {

      private static final String DOCKER_IMAGE = "ghcr.io/kaiwinter/wildfly27-mgmt-user:latest";

      private static final int WILDFLY_HTTP_PORT = 8080;
      private static final int WILDFLY_MANAGEMENT_PORT = 9990;

      /**
       * Method which observes {@link ContainerRegistry}. Gets called by Arquillian at startup time.
       *
       * @param registry
       *           contains containers defined in arquillian.xml
       * @param serviceLoader
       */
      public void registerInstance(@Observes ContainerRegistry registry, ServiceLoader serviceLoader) {
         GenericContainer dockerContainer = new GenericContainer(DOCKER_IMAGE) //
               .withExposedPorts(WILDFLY_HTTP_PORT, WILDFLY_MANAGEMENT_PORT);
         dockerContainer.start();

         configureArquillianForRemoteWildfly(dockerContainer, registry);
      }

      private void configureArquillianForRemoteWildfly(GenericContainer dockerContainer, ContainerRegistry registry) {
         Integer wildflyHttpPort = dockerContainer.getMappedPort(WILDFLY_HTTP_PORT);
         Integer wildflyManagementPort = dockerContainer.getMappedPort(WILDFLY_MANAGEMENT_PORT);

         String containerIpAddress = dockerContainer.getHost();
         Container arquillianContainer = registry.getContainers().iterator().next();
         ContainerDef containerConfiguration = arquillianContainer.getContainerConfiguration();
         containerConfiguration.property(ContainerConfiguration.MANAGEMENT_ADDRESS_KEY, containerIpAddress);
         containerConfiguration.property(ContainerConfiguration.MANAGEMENT_PORT_KEY,
               String.valueOf(wildflyManagementPort));
         containerConfiguration.property(ContainerConfiguration.USERNAME_KEY, "admin");
         containerConfiguration.property(ContainerConfiguration.PASSWORD_KEY, "Admin#007");

         WildflyDockerExtension.baseUrl = "http://" + containerIpAddress + ":" + wildflyHttpPort
               + "/testcontainers-jsf/";
      }

   }
}