package com.github.kaiwinter.testcontainers.hibernate.db.entity;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link UserTest}. The <code>persistence.xml</code> uses the special database driver
 * {@link org.testcontainers.jdbc.ContainerDatabaseDriver} from the testcontainers library. This driver will start a
 * Docker container with a MySQL database. Hibernate then creates a Persistence Unit for this docker-database.
 */
public final class UserTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(UserTest.class);

   private static EntityManager entityManager = Persistence.createEntityManagerFactory("TestPU").createEntityManager();

   @Test
   public void testSaveAndLoad() {
      User user = new User("user 1");

      entityManager.getTransaction().begin();
      entityManager.persist(user);
      LOGGER.info("User persisted with ID {}", user.getId());

      User userFromDb = entityManager.find(User.class, user.getId());

      assertEquals(user.getUsername(), userFromDb.getUsername());
      entityManager.getTransaction().rollback();
   }
}
