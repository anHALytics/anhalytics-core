package fr.inria.anhalytics.dao.anhalytics;

import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.dao.DatabaseConnection;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.In_SerialDAO;
import fr.inria.anhalytics.dao.AddressDAO;
import fr.inria.anhalytics.dao.PublisherDAO;
import fr.inria.anhalytics.dao.MonographDAO;
import fr.inria.anhalytics.dao.PublicationDAO;
import fr.inria.anhalytics.dao.Conference_EventDAO;
import fr.inria.anhalytics.dao.anhalytics.LocationDAO;
import fr.inria.anhalytics.dao.anhalytics.OrganisationDAO;
import fr.inria.anhalytics.dao.PersonDAO;
import fr.inria.anhalytics.dao.anhalytics.AffiliationDAO;
import fr.inria.anhalytics.dao.anhalytics.Document_IdentifierDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class DAOFactory extends AbstractDAOFactory {

    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);

    protected static Connection conn = null;

    public static void initConnection() {
        conn = DatabaseConnection.getInstance(DatabaseConnection.anhalytics_dbName);
    }
    public DAO getDocumentDAO() {
        return new DocumentDAO(conn);
    }

    public DAO getAddressDAO() {
        return new AddressDAO(conn);
    }

    public DAO getAffiliationDAO() {
        return new AffiliationDAO(conn);
    }

    public DAO getConference_EventDAO() {
        return new Conference_EventDAO(conn);
    }

    public DAO getDocument_IdentifierDAO() {
        return new Document_IdentifierDAO(conn);
    }

    public DAO getIn_SerialDAO() {
        return new In_SerialDAO(conn);
    }

    public DAO getLocationDAO() {
        return new LocationDAO(conn);
    }

    public DAO getMonographDAO() {
        return new MonographDAO(conn);
    }

    public DAO getOrganisationDAO() {
        return new OrganisationDAO(conn);
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
            logger.debug("The autocommit was disabled!");
        } catch (SQLException e) {
            logger.error("There was an error disabling autocommit");
        }
    }

    public void endTransaction() {
        try {
            conn.commit();
            logger.debug("The transaction was successfully executed");
        } catch (SQLException ex) {
            logger.error("Error happened while commiting the changes.");
        }
    }

    public void rollback() {
        try {
                // We rollback the transaction, to the last SavePoint!
            conn.rollback();
            logger.debug("The transaction was rollback.");
        } catch (SQLException e1) {
            logger.error("There was an error making a rollback");

        }
    }
    public static void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
