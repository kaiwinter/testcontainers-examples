package com.github.kaiwinter.user.view;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.github.kaiwinter.testsupport.arquillian.WildflyDockerExtension;
import com.github.kaiwinter.user.User;
import com.github.kaiwinter.user.service.UserService;

/**
 * Tests the users.xhtml view.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class UserViewTest {

   @Drone
   private WebDriver driver;

   @Deployment
   public static WebArchive createDeployment() {
      WebArchive war = ShrinkWrap.create(WebArchive.class) //
            .addClasses(UserView.class, UserService.class, User.class) //
            .addAsWebResource(new File("src/main/webapp/users.xhtml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml")) //
            .addAsLibraries(Maven.resolver() //
                  .loadPomFromFile("pom.xml") //
                  .importCompileAndRuntimeDependencies() //
                  .resolve().withoutTransitivity().asFile());

      return war;
   }

   /**
    * Tests if the xhtml page contains a table with five rows.
    */
   @Test
   public void tableContainsData() {
      String address = WildflyDockerExtension.baseUrl + "users.xhtml";
      driver.get(address);
      WebElement datatable = driver.findElement(By.className("ui-datatable-data"));

      List<WebElement> datatableRows = datatable.findElements(By.className("ui-widget-content"));
      assertEquals(5, datatableRows.size());
   }
}
