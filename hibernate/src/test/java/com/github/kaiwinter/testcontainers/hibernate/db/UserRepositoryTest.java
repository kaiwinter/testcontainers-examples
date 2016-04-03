package com.github.kaiwinter.testcontainers.hibernate.db;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaiwinter.instantiator.InjectionObjectFactory;
import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;

public final class UserRepositoryTest {
   private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryTest.class);

   @Test
   public void testSaveAndLoad() {
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
