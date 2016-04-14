package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.kb.properties.KbProperties;
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

                connectDB = DriverManager.getConnection(KbProperties.getMysql_url() + KbProperties.getMysql_db(), KbProperties.getMysql_user(), KbProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectDB;
    }
    
    public static Connection getBiblioDBInstance() {
        try {
            if (connectBiblioDB == null ) {

                connectBiblioDB = DriverManager.getConnection(KbProperties.getMysql_url() + KbProperties.getMysql_bibliodb(), KbProperties.getMysql_user(), KbProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectBiblioDB;
    }
    
}
