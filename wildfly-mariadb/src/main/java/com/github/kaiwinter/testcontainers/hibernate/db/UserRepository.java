package com.github.kaiwinter.testcontainers.hibernate.db;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;

public final class UserRepository implements IUserRepository {

   @PersistenceContext
   private EntityManager entityManager;

   @Override
   public User find(int id) {
      return entityManager.find(User.class, id);
   }

   @Override
   public Collection<User> findAll() {
      Query query = entityManager.createQuery("SELECT u FROM " + User.class.getSimpleName() + " u");
      return query.getResultList();
   }

   @Override
   public User save(User user) {
      entityManager.persist(user);
      return user;
   }

   @Override
   public void delete(User user) {
      entityManager.remove(user);
   }
}
