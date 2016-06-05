package com.github.kaiwinter.testcontainers.wildfly.db;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

public final class UserRepository {

   @PersistenceContext
   private EntityManager entityManager;

   public User find(int id) {
      return entityManager.find(User.class, id);
   }

   public Collection<User> findAll() {
      Query query = entityManager.createQuery("SELECT u FROM User u");
      return query.getResultList();
   }

   public User save(User user) {
      entityManager.persist(user);
      return user;
   }

   public void delete(User user) {
      entityManager.remove(user);
   }
}
