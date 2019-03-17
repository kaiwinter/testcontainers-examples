package com.github.kaiwinter.user.view;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
