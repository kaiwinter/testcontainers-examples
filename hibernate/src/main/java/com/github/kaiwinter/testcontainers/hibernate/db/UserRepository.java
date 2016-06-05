package com.github.kaiwinter.testcontainers.hibernate.db;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;

/**
 * A database repository which got infected with business logic which should be tested.
 */
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

   /**
    * Load all users with other names than 'admin' and 'root' unless the user's login_count is the max count.
    *
    * @return non-admin users
    */
   public Collection<User> findAllNonAdmin() {
      int maxLoginCount = (int) entityManager.createQuery("SELECT MAX(loginCount) FROM User").getSingleResult();
      Query query = entityManager.createQuery(
         "SELECT u FROM User u WHERE username NOT IN ('root', 'admin') OR (username IN ('root', 'admin') AND loginCount=?)");
      query.setParameter(1, maxLoginCount);
      return query.getResultList();
   }
}
