package com.github.kaiwinter.testcontainers.wildfly.core;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

/**
 * A demo service which does something testable and relies on a database class which needs to be injected by the
 * container.
 */
@ApplicationScoped
public class UserService {

   @Inject
   private UserRepository userRepository;

   /**
    * Loads all users from the database and calculates the number of total logins.
    * 
    * @return number of logins of all users
    */
   public int calculateSumOfLogins() {
      Collection<User> users = userRepository.findAll();

      int sumOfLogins = users.stream().mapToInt(User::getLoginCount).sum();
      return sumOfLogins;
   }
}
