package com.github.kaiwinter.testcontainers.hibernate.db;

import java.util.Collection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;

/**
 * A database repository which has test-worthy logic.
 */
public final class UserRepository {

   @PersistenceContext
   private EntityManager entityManager;

   public User find(int id) {
      return entityManager.find(User.class, id);
   }

   public User findByUsername(String username) {
      TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE username=?1", User.class);
      query.setParameter(1, username);
      return query.getSingleResult();
   }

   public Collection<User> findAll() {
      TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u", User.class);
      return query.getResultList();
   }

   public User save(User user) {
      entityManager.persist(user);
      return user;
   }

   public void delete(User user) {
      entityManager.remove(user);
   }

   /**
    * Resets the login count for other users than root and admin.
    */
   public void resetLoginCountForUsers() {
      Query query = entityManager.createQuery("UPDATE User SET loginCount=0 WHERE username NOT IN ('root', 'admin')");
      query.executeUpdate();
   }
}
