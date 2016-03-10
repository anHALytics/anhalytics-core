package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.ingest.properties.IngestProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static Connection connectDB;
    private static Connection connectBiblioDB;
    
    public static Connection getDBInstance() {
        try {
            if (connectDB == null ) {

                connectDB = DriverManager.getConnection(IngestProperties.getMysql_url() + IngestProperties.getMysql_db(), IngestProperties.getMysql_user(), IngestProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectDB;
    }
    
    public static Connection getBiblioDBInstance() {
        try {
            if (connectBiblioDB == null ) {

                connectBiblioDB = DriverManager.getConnection(IngestProperties.getMysql_url() + IngestProperties.getMysql_bibliodb(), IngestProperties.getMysql_user(), IngestProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectBiblioDB;
    }
    
}
