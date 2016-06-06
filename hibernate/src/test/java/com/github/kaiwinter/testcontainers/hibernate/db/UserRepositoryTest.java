package com.github.kaiwinter.testcontainers.hibernate.db;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.instantiator.InjectionObjectFactory;
import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;
import com.github.kaiwinter.testcontainers.hibernate.db.entity.UserTest;
import com.github.kaiwinter.testsupport.db.DockerDatabaseTestUtil;

/**
 * Tests for {@link UserRepository}. The <code>persistence.xml</code> uses the special database driver
 * {@link org.testcontainers.jdbc.ContainerDatabaseDriver} from the testcontainers library. This driver will start a
 * Docker container with a MySQL database. Hibernate then creates a Persistence Unit for this docker-database. The
 * library di-instantiator is used to place the Persistence Unit in the {@link UserRepository} to test its logic.
 *
 * @see {@link UserTest}
 */
public final class UserRepositoryTest {
   private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryTest.class);

   private static EntityManager entityManager = Persistence.createEntityManagerFactory("TestPU", null)
      .createEntityManager();

   private static UserRepository userRepository;

   static {
      InjectionObjectFactory factory = new InjectionObjectFactory(PersistenceContext.class);
      factory.setImplementationForClassOrInterface(EntityManager.class, entityManager);
      userRepository = factory.getInstance(UserRepository.class);
   }

   @Before
   public void setup() {
      // Rolling back transaction will make tests after a failed test run correctly
      if (entityManager.getTransaction().isActive()) {
         entityManager.getTransaction().rollback();
      }
      // Clear hibernate cache, else inserted testdata may cause trouble
      entityManager.clear();
   }

   /**
    * Loads a user by username.
    */
   @Test
   public void testFind() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));

      User user = userRepository.findByUsername("admin");
      assertEquals(3, user.getLoginCount());
   }

   /**
    * Loads all users and counts the result.
    */
   @Test
   public void testFindAll() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));

      Collection<User> findAll = userRepository.findAll();
      assertEquals(3, findAll.size());
   }

   /**
    * Deletes one User and counts the remaining users.
    */
   @Test
   public void testDelete() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));

      User user = userRepository.findByUsername("user");
      entityManager.getTransaction().begin();
      userRepository.delete(user);
      entityManager.getTransaction().commit();

      assertEquals(2, userRepository.findAll().size());
   }

   /**
    * Creates a new User and re-loads it by its ID. Checks the username of the original User against the loaded one from
    * the database.
    */
   @Test
   public void testSave() {
      User user = new User();
      user.setUsername("user 1");

      entityManager.getTransaction().begin();
      userRepository.save(user);
      entityManager.getTransaction().commit();

      LOGGER.info("User persisted with ID {}", user.getId());

      User userFromDb = userRepository.find(user.getId());
      assertEquals(user.getUsername(), userFromDb.getUsername());
   }

   @Test
   public void testResetLoginCountForUsers() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));

      entityManager.getTransaction().begin();
      userRepository.resetLoginCountForUsers();
      entityManager.getTransaction().commit();

      User rootUser = userRepository.findByUsername("root");
      User adminUser = userRepository.findByUsername("admin");
      User user = userRepository.findByUsername("user");
      assertEquals(5, rootUser.getLoginCount());
      assertEquals(3, adminUser.getLoginCount());
      assertEquals(0, user.getLoginCount());
   }

}
