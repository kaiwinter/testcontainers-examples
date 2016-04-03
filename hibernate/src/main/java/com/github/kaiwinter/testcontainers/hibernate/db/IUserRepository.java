package com.github.kaiwinter.testcontainers.hibernate.db;

import java.util.Collection;

import com.github.kaiwinter.testcontainers.hibernate.db.entity.User;

public interface IUserRepository {

   User find(int id);

   Collection<User> findAll();

   User save(User user);

   void delete(User user);
}
