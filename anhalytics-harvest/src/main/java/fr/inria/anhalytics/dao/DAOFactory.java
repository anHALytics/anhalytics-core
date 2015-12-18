package fr.inria.anhalytics.dao;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class DAOFactory extends AbstractDAOFactory {

    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);

    protected static final Connection conn = AnhalyticsConnection.getInstance();

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
