package com.github.kaiwinter.testsupport.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.persistence.EntityManager;
import javax.script.ScriptException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.jdbc.ext.ScriptUtils;
import org.xml.sax.InputSource;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Helper class to insert test data into a database.
 */
public abstract class DockerDatabaseTestUtil {

   private static Logger LOGGER = LoggerFactory.getLogger(DockerDatabaseTestUtil.class);

   /**
    * Executes the passed sql files on the EntityManager.
    *
    * @param entityManager
    *           the EntityManager
    * @param sqlScripts
    *           sql scripts to execute
    */
   public static void insertSqlFile(EntityManager entityManager, final File... sqlScripts) {
      entityManager.unwrap(Session.class).doWork(new Work() {

         @Override
         public void execute(Connection connection) throws SQLException {
            // Setup database
            try {
               for (File file : sqlScripts) {
                  LOGGER.debug("INSERTing {}", file.getAbsolutePath());
                  Assume.assumeTrue("SQL-Script not found", file.isFile());
                  String sql = Resources.toString(file.toURI().toURL(), Charsets.UTF_8);
                  executeSqlScript(connection, file.getName(), sql);
                  LOGGER.debug("INSERTing {} ... done", file.getAbsolutePath());
               }
            } catch (IOException | ScriptException e) {
               throw new SQLException(e);
            }
         }
      });
   }

   /**
    * Executes the passed sql queries on the EntityManager.
    *
    * @param entityManager
    *           the EntityManager
    *
    * @param sqlQuery
    *           queries to execute
    */
   public static void insertSqlString(EntityManager entityManager, final String... sqlQuery) {
      entityManager.unwrap(Session.class).doWork(new Work() {

         @Override
         public void execute(Connection connection) throws SQLException {
            // Setup database
            try {
               for (String query : sqlQuery) {
                  LOGGER.debug("INSERTing '{}'", query);
                  executeSqlScript(connection, null, query);
                  LOGGER.debug("INSERTing '{}' ... done", query);
               }
            } catch (ScriptException e) {
               throw new SQLException(e);
            }
         }
      });
   }

   private static void executeSqlScript(Connection connection, String scriptName, String sqlString)
      throws ScriptException {
      boolean continueOnError = false;
      boolean ignoreFailedDrops = true;
      ScriptUtils.executeSqlScript(connection, scriptName, sqlString, continueOnError, ignoreFailedDrops, //
         ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
         ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
   }

   /**
    * Inserts test data in DbUnit XML format.
    *
    * @param entityManager
    *           the EntityManager
    * @param dbUnitTestdata
    *           test file stream
    */
   public static void insertDbUnitTestdata(EntityManager entityManager, final InputStream dbUnitTestdata) {

      entityManager.unwrap(Session.class).doWork(new Work() {

         @Override
         public void execute(Connection connection) throws SQLException {
            // Insert Testdata
            try {
               LOGGER.debug("INSERTing testdata");
               DatabaseConnection databaseConnection = new DatabaseConnection(connection);
               databaseConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                  new MySqlDataTypeFactory());

               FlatXmlDataSet dataSet = new FlatXmlDataSet(
                  new FlatXmlProducer(new InputSource(dbUnitTestdata), false, true));
               DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, dataSet);
               LOGGER.debug("INSERTing testdata ... done");
            } catch (DatabaseUnitException e) {
               throw new SQLException(e);
            }
         }

      });
   }
}
