package com.github.kaiwinter.testcontainers.wildfly.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Collection;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.kaiwinter.testcontainers.wildfly.core.UserService;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;
import com.github.kaiwinter.testsupport.arquillian.WildflyMariaDBDockerExtension;
import com.github.kaiwinter.testsupport.db.DockerDatabaseTestUtil;

/**
 * Tests for {@link UserRepository}. The class {@link WildflyMariaDBDockerExtension} registers an arqullian observer
 * which is called on initialization time. The observer starts a Docker container with Wildfly 10/MariaDB and inserts
 * some test data. Arquillian then deploys to that server and runs this tests.
 */
@ExtendWith(ArquillianExtension.class)
public final class UserRepositoryTest {

   @Inject
   private UserRepository userRepository;

   @Inject
   private UserTransaction transaction;

   @PersistenceContext
   private EntityManager entityManager;

   @Deployment
   public static EnterpriseArchive createDeployment() {
      WebArchive war = ShrinkWrap.create(WebArchive.class) //
         .addClasses(UserService.class, UserRepository.class, User.class) //
         .addClasses(UserRepositoryTest.class, DockerDatabaseTestUtil.class) //
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

   /**
    * Loads a user by its ID.
    */
   @Test
   void testFind() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));
      User user = userRepository.findByUsername("admin");
      assertEquals(3, user.getLoginCount());
   }

   /**
    * Loads all users and counts the result.
    */
   @Test
   void testFindAll() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));
      Collection<User> findAll = userRepository.findAll();
      assertEquals(3, findAll.size());
   }

   /**
    * Deletes one User and counts the remaining users.
    */
   @Test
   void testDelete() throws NotSupportedException, SystemException {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));
      transaction.begin();
      User user = userRepository.findByUsername("admin");
      userRepository.delete(user);
      assertEquals(2, userRepository.findAll().size());
      transaction.rollback();
   }

   /**
    * Creates a new User and counts the number of total users.
    */
   @Test
   void testSave() throws NotSupportedException, SystemException {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));
      transaction.begin();
      User user = new User();
      user.setUsername("test-user");
      userRepository.save(user);
      assertEquals(4, userRepository.findAll().size());
      transaction.rollback();
   }
}
