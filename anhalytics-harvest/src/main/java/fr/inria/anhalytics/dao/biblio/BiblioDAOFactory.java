/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.anhalytics.dao.biblio;

import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.DatabaseConnection;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.dao.PublisherDAO;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author achraf
 */
public class BiblioDAOFactory extends AbstractBiblioDAOFactory {

    private static final Logger logger = LoggerFactory.getLogger(BiblioDAOFactory.class);
    protected static Connection conn = null;

    public static void initConnection() {
        conn = DatabaseConnection.getInstance(DatabaseConnection.anhalytics_biblio_dbName);
    }

    public DAO getDocumentDAO() {
        return new DocumentDAO(conn);
    }

    public DAO getAddressDAO() {
        return new AddressDAO(conn);
    }

    public DAO getConference_EventDAO() {
        return new Conference_EventDAO(conn);
    }

    public DAO getIn_SerialDAO() {
        return new In_SerialDAO(conn);
    }

    public DAO getMonographDAO() {
        return new MonographDAO(conn);
    }

    public DAO getPersonDAO() {
        return new PersonDAO(conn);
    }

    public DAO getPublicationDAO() {
        return new PublicationDAO(conn);
    }

    public DAO getPublisherDAO() {
        return new PublisherDAO(conn);
    }

    public void openTransaction() {
        try {
            conn.setAutoCommit(false);
            logger.info("The autocommit was disabled!");
        } catch (SQLException e) {
            logger.error("There was an error disabling autocommit");
        }
    }

    public void endTransaction() {
        try {
            conn.commit();
            logger.info("The transaction was successfully executed");
        } catch (SQLException ex) {
            logger.error("Error happened while commiting the changes.");
        }
    }

    public void rollback() {
        try {
                // We rollback the transaction, to the last SavePoint!
            conn.rollback();
            logger.info("The transaction was rollback.");
        } catch (SQLException e1) {
            logger.error("There was an error making a rollback");

        }
    }
}
