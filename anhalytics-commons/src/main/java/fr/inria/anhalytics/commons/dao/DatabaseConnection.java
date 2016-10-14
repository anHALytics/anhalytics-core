package fr.inria.anhalytics.commons.dao;

import fr.inria.anhalytics.commons.exceptions.PropertyException;
import fr.inria.anhalytics.commons.properties.CommonsProperties;
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

                connectDB = DriverManager.getConnection("jdbc:mysql://"+CommonsProperties.getMysql_host() +
                (CommonsProperties.getMysql_port().isEmpty() ? "":":" + CommonsProperties.getMysql_port())+"/"+ CommonsProperties.getMysql_db(), CommonsProperties.getMysql_user(), CommonsProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectDB;
    }
    
    public static Connection getBiblioDBInstance() {
        try {
            if (connectBiblioDB == null ) {

                connectBiblioDB = DriverManager.getConnection("jdbc:mysql://"+CommonsProperties.getMysql_host() +
                (CommonsProperties.getMysql_port().isEmpty() ? "":":" + CommonsProperties.getMysql_port())+"/"+ CommonsProperties.getMysql_bibliodb(), CommonsProperties.getMysql_user(), CommonsProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectBiblioDB;
    }
    
}
