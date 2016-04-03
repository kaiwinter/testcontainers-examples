package com.github.kaiwinter.testcontainers.hibernate.db.entity;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(UserTest.class);

   private static EntityManager entityManager = Persistence.createEntityManagerFactory("TestPU").createEntityManager();

   @Test
   public void testSaveAndLoad() {
      User user = new User("user 1");

      entityManager.getTransaction().begin();
      entityManager.persist(user);
      entityManager.getTransaction().commit();
      LOGGER.info("User persisted with ID {}", user.getId());

      User userFromDb = entityManager.find(User.class, user.getId());

      assertEquals(user.getUsername(), userFromDb.getUsername());
   }
}
