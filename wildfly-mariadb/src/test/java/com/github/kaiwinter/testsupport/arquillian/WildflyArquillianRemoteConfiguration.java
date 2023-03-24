package com.github.kaiwinter.testsupport.arquillian;

/**
 * Defines configuration property names of the Arquillian adapter "wildfly-arquillian-container-remote".
 */
public final class WildflyArquillianRemoteConfiguration {

   private WildflyArquillianRemoteConfiguration() {
      // should not be instantiated
   }

   /**
    * Remote Wildfly configuration, host, port and admin account as well as the servlet protocol.
    */
   public static final class ContainerConfiguration {

      /** The IP address of the remote Wildfly server. */
      public static final String MANAGEMENT_ADDRESS_KEY = "managementAddress";

      /** The management port of the remote Wildfly server (which is 9990 by default). */
      public static final String MANAGEMENT_PORT_KEY = "managementPort";

      /** The management account user name of the remote Wildfly server. */
      public static final String USERNAME_KEY = "username";

      /** The management account password of the remote Wildfly server. */
      public static final String PASSWORD_KEY = "password";

      /**
       * The protocol to use for the communication in the test.
       */
      public static final class ServletProtocolDefinition {
         /** Arquillian's name for the Servlet protocol. */
         public static final String NAME = "Servlet 5.0";

         /** The IP of the remote Wildfly server. */
         public static final String HOST_KEY = "host";

         /** The IP of the remote Wildfly (which is 8080 by default. */
         public static final String PORT_KEY = "port";
      }
   }

}
