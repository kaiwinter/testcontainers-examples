package com.github.kaiwinter.testcontainers.wildfly.db;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import javax.inject.Inject;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;
import com.github.kaiwinter.testsupport.arquillian.WildflyMariaDBDockerExtension;

/**
 * Tests for {@link UserRepository}. The class {@link WildflyMariaDBDockerExtension} registers an arqullian observer
 * which is called on initialization time. The observer starts a Docker container with Wildfly 10/MariaDB and inserts
 * some test data. Arquillian then deploys to that server and runs this tests.
 */
@RunWith(Arquillian.class)
public final class UserRepositoryTest {

   @Deployment
   public static JavaArchive createDeployment() {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class) //
         .addClasses(UserRepository.class, User.class) //
         .addAsResource("META-INF/persistence.xml") //
         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
      return jar;
   }

   @Inject
   private UserRepository userRepository;

   @Inject
   private UserTransaction transaction;

   /**
    * Loads a user by its ID.
    */
   @Test
   public void testFind() {
      User user = userRepository.find(2);
      assertEquals("admin", user.getUsername());
   }

   /**
    * Loads all users and counts the result.
    */
   @Test
   public void testFindAll() {
      Collection<User> findAll = userRepository.findAll();
      assertEquals(3, findAll.size());
   }

   /**
    * Deletes one User and counts the remaining users.
    */
   @Test
   public void testDelete() throws NotSupportedException, SystemException {
      transaction.begin();
      User user = userRepository.find(2);
      userRepository.delete(user);
      assertEquals(2, userRepository.findAll().size());
      transaction.rollback();
   }

   /**
    * Creates a new User and counts the number of total users.
    */
   @Test
   public void testSave() throws NotSupportedException, SystemException {
      transaction.begin();
      User user = new User();
      user.setUsername("test-user");
      userRepository.save(user);
      assertEquals(4, userRepository.findAll().size());
      transaction.rollback();
   }
}
