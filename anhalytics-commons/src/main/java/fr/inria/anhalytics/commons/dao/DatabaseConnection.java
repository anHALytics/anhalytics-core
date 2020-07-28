package fr.inria.anhalytics.commons.dao;

import fr.inria.anhalytics.commons.exceptions.ServiceException;
import fr.inria.anhalytics.commons.properties.CommonsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author azhar
 */
public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);
    private static Connection connectDB;
    private static Connection connectBiblioDB;

    public static Connection getDBInstance() {
        try {
            if (connectDB == null) {

                final String mysqlPort = CommonsProperties.getMysql_port().isEmpty() ? "" : ":" + CommonsProperties.getMysql_port();
                final String url = "jdbc:mysql://"
                        + CommonsProperties.getMysql_host() +
                        mysqlPort + "/" + CommonsProperties.getMysql_db() + "?characterEncoding=utf8";

                connectDB = DriverManager.getConnection(url, CommonsProperties.getMysql_user(), CommonsProperties.getMysql_pass());
            }
        } catch (SQLException e) {
            throw new ServiceException("Can't connect to MySQL. ", e);
        }
        return connectDB;
    }

    public static Connection getBiblioDBInstance() {
        try {
            if (connectBiblioDB == null) {

                final String mysqlPort = CommonsProperties.getMysql_port().isEmpty() ? "" : ":" + CommonsProperties.getMysql_port();
                final String url = "jdbc:mysql://"
                        + CommonsProperties.getMysql_host() +
                        mysqlPort + "/" + CommonsProperties.getMysql_bibliodb() + "?characterEncoding=utf8";

                connectBiblioDB = DriverManager.getConnection(url, CommonsProperties.getMysql_user(), CommonsProperties.getMysql_pass());

            }
        } catch (SQLException e) {
            throw new ServiceException("Can't connect to MySQL. ", e);
        }
        return connectBiblioDB;
    }

}
