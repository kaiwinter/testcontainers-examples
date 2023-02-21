package com.github.kaiwinter.testcontainers.hibernate.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {

   @Id
   @Column(name = "id", unique = true, nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;

   @Column(name = "username", nullable = false)
   private String username;

   @Column(name = "login_count", nullable = false)
   private int loginCount;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public int getLoginCount() {
      return loginCount;
   }

   public void setLoginCount(int loginCount) {
      this.loginCount = loginCount;
   }
}
