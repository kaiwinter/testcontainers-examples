package com.github.kaiwinter.testcontainers.wildfly.core;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.UserRepositoryTest;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

/**
 * Tests for a service layer which depends on the database.
 * 
 * @see {@link UserRepositoryTest}.
 */
@RunWith(Arquillian.class)
public final class UserServiceTest {

   @Deployment
   public static JavaArchive createDeployment() {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class) //
         .addClasses(UserService.class, UserRepository.class, User.class) //
         .addAsResource("META-INF/persistence.xml") //
         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
      return jar;
   }

   @Inject
   private UserService userService;

   @Test
   public void testSumOfLogins() {
      int sumOfLogins = userService.calculateSumOfLogins();
      assertEquals(9, sumOfLogins);
   }
}
