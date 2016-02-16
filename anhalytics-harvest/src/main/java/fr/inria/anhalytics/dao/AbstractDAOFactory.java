package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.dao.anhalytics.DAOFactory;

/**
 *
 * @author azhar
 */
public abstract class AbstractDAOFactory {

    public static final int DAO_FACTORY = 0;

    public static final int MONGO_DAO_FACTORY = 1;

    public abstract DAO getDocumentDAO();

    public abstract DAO getPublicationDAO();

    public abstract DAO getMonographDAO();

    public abstract DAO getPublisherDAO();

    public abstract DAO getAddressDAO();

    public abstract DAO getAffiliationDAO();

    public abstract DAO getConference_EventDAO();

    public abstract DAO getDocument_IdentifierDAO();

    public abstract DAO getIn_SerialDAO();

    public abstract DAO getLocationDAO();

    public abstract DAO getDocument_OrganisationDAO();
    
    public abstract DAO getOrganisationDAO();

    public abstract DAO getPersonDAO();
    
    public abstract void openTransaction();
    
    public abstract void endTransaction();
    
    public abstract void rollback();

    public static AbstractDAOFactory getFactory(int type) {
        switch (type) {
            case DAO_FACTORY:
                return new DAOFactory();
            case MONGO_DAO_FACTORY:
                return new MongoDAOFactory();
            default:
                return null;
        }
    }
}
