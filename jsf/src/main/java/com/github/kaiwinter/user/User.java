package com.github.kaiwinter.user;

/**
 * A user.
 */
public class User {

   private final int id;

   private final String name;

   /**
    * Constructs a new User.
    *
    * @param id
    *           the User ID
    * @param name
    *           the User name
    */
   public User(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return id;
   }

   public String getName() {
      return name;
   }
}
