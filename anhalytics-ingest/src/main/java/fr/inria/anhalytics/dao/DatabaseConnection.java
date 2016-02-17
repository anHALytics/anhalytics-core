package fr.inria.anhalytics.dao;

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
    public static String anhalytics_dbName = "anhalytics";
    public static String anhalytics_biblio_dbName = "anhalytics_biblio";
    private static String url = "jdbc:mysql://localhost:3306/";
    private static String user = "root";
    private static String passwd = "";
    private static Connection connect;

    public static Connection getInstance(String dbName) {
        if (connect == null) {
            try {
                connect = DriverManager.getConnection(IngestProperties.getMysql_url() + dbName, IngestProperties.getMysql_user(), IngestProperties.getMysql_pass());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connect;
    }
}
