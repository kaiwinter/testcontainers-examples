package com.github.kaiwinter.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import com.github.kaiwinter.user.User;

/**
 * Service for producing some mock data.
 */
@ApplicationScoped
public class UserService {

   /**
    * Creates a {@link List} of {@link User}s. The number of users is determined by the passed
    * <code>numberOfUsers</code> parameter.
    *
    * <p>
    * <b>This method is just for the purpose of this demo.</b>
    * </p>
    *
    * @param numberOfUsers
    *           the numbers of Users to create
    * @return {@link List} of {@link User}s
    */
   public List<User> getTopUsers(int numberOfUsers) {
      List<User> users = new ArrayList<>();
      IntStream.range(0, numberOfUsers).forEach(counter -> users.add(new User(counter, "User " + counter)));

      return users;
   }

}
