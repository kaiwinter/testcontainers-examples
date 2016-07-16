package com.github.kaiwinter.testcontainers.wildfly.core;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.UserRepositoryTest;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;
import com.github.kaiwinter.testsupport.db.DockerDatabaseTestUtil;

/**
 * Tests for a service layer which depends on the database.
 * 
 * @see {@link UserRepositoryTest}.
 */
@RunWith(Arquillian.class)
public final class UserServiceTest {

   @Inject
   private UserService userService;

   @PersistenceContext
   private EntityManager entityManager;

   @Deployment
   public static EnterpriseArchive createDeployment() {
      WebArchive war = ShrinkWrap.create(WebArchive.class) //
         .addClasses(UserService.class, UserRepository.class, User.class) //
         .addClasses(UserServiceTest.class, DockerDatabaseTestUtil.class) //
         .addAsResource("META-INF/persistence.xml") //
         .addAsResource("testdata.xml") //
         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      File[] pomDependencies = Maven.resolver() //
         .loadPomFromFile("pom.xml").importDependencies(ScopeType.TEST) //
         .resolve().withTransitivity().asFile();

      EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class) //
         .addAsModule(war) //
         .addAsLibraries(pomDependencies);

      return ear;
   }

   @Test
   public void testSumOfLogins() {
      DockerDatabaseTestUtil.insertDbUnitTestdata(entityManager, getClass().getResourceAsStream("/testdata.xml"));
      int sumOfLogins = userService.calculateSumOfLogins();
      assertEquals(9, sumOfLogins);
   }
}
