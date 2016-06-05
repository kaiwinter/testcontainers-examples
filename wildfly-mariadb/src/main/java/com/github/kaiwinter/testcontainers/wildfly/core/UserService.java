package com.github.kaiwinter.testcontainers.wildfly.core;

import java.util.Collection;

import javax.inject.Inject;

import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

/**
 * A demo service which does something testable and relies on a database class which needs to be injected by the
 * container.
 */
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

      int sumOfLogins = users.stream().mapToInt(user -> user.getLoginCount()).sum();
      return sumOfLogins;
   }
}
