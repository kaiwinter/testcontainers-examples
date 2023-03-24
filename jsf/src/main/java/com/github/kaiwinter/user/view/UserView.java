package com.github.kaiwinter.user.view;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.github.kaiwinter.user.User;
import com.github.kaiwinter.user.service.UserService;

/**
 * View Bean for users.xhtml.
 */
@Named
@RequestScoped
public class UserView {

   @Inject
   private UserService userService;

   /**
    * Loads the top 5 users.
    *
    * @return the top 5 users
    */
   public List<User> getUsers() {
      return userService.getTopUsers(5);
   }
}
