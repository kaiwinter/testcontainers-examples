package com.github.kaiwinter.testcontainers.hibernate.db;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.instantiator.InjectionObjectFactory;
import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;
import com.github.kaiwinter.testcontainers.hibernate.db.entity.UserTest;

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

   /**
    * Loads a user by its ID.
    */
   @Test
   public void testFind() {
      User user = getCleanUserRepository().find(2);
      assertEquals("admin", user.getUsername());
   }

   /**
    * Loads all users and counts the result.
    */
   @Test
   public void testFindAll() {
      Collection<User> findAll = getCleanUserRepository().findAll();
      assertEquals(3, findAll.size());
   }

   /**
    * Deletes one User and counts the remaining users.
    */
   @Test
   public void testDelete() {
      UserRepository userRepository = getCleanUserRepository();
      User user = userRepository.find(2);
      userRepository.delete(user);
      assertEquals(2, userRepository.findAll().size());
   }

   /**
    * Creates a new User and re-loads it by its database ID. Then the username of the original User object is checked
    * against the loaded one from the database.
    */
   @Test
   public void testSave() {
      UserRepository userRepository = getCleanUserRepository();

      User user = new User("user 1");
      userRepository.save(user);

      LOGGER.info("User persisted with ID {}", user.getId());

      User userFromDb = userRepository.find(user.getId());
      assertEquals(user.getUsername(), userFromDb.getUsername());
   }

   private UserRepository getCleanUserRepository() {
      EntityManager entityManager = Persistence.createEntityManagerFactory("TestPU", null).createEntityManager();
      entityManager.getTransaction().begin();

      InjectionObjectFactory factory = new InjectionObjectFactory(PersistenceContext.class);
      factory.setImplementationForClassOrInterface(EntityManager.class, entityManager);

      UserRepository userRepository = factory.getInstance(UserRepository.class);
      return userRepository;
   }
}
