package com.github.kaiwinter.testcontainers.hibernate.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user", catalog = "test")
public class User implements Serializable {

   private int id;
   private String username;

   public User() {
   }

   public User(String username) {
      this.username = username;
   }

   @Id
   @Column(name = "id", unique = true, nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   @Column(name = "username", nullable = false, length = 65535)
   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

}
