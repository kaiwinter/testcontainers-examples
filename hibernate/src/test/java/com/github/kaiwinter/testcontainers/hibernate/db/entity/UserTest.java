package com.github.kaiwinter.testcontainers.hibernate.db.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link UserTest}. The <code>persistence.xml</code> uses the special database driver
 * {@link org.testcontainers.jdbc.ContainerDatabaseDriver} from the testcontainers library. This driver will start a
 * Docker container with a MySQL database. Hibernate then creates a Persistence Unit for this docker-database.
 */
final class UserTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(UserTest.class);

   private static EntityManager entityManager = Persistence.createEntityManagerFactory("TestPU").createEntityManager();

   @Test
   void testSaveAndLoad() {
      User user = new User();
      user.setUsername("user 1");

      entityManager.getTransaction().begin();
      entityManager.persist(user);
      entityManager.getTransaction().commit();
      LOGGER.info("User persisted with ID {}", user.getId());

      User userFromDb = entityManager.find(User.class, user.getId());

      assertEquals(user.getUsername(), userFromDb.getUsername());
   }
}
