package fr.inria.anhalytics.ingest.dao.biblio;

import fr.inria.anhalytics.dao.DAO;


/**
 *
 * @author azhar
 */
public abstract class AbstractBiblioDAOFactory {

    public static final int DAO_FACTORY = 0;

    public static final int MONGO_DAO_FACTORY = 1;

    public abstract DAO getDocumentDAO();

    public abstract DAO getPublicationDAO();

    public abstract DAO getMonographDAO();

    public abstract DAO getPublisherDAO();

    public abstract DAO getAddressDAO();

    public abstract DAO getConference_EventDAO();

    public abstract DAO getIn_SerialDAO();

    public abstract DAO getPersonDAO();
    
    public abstract void openTransaction();
    
    public abstract void endTransaction();
    
    public abstract void rollback();

    public static AbstractBiblioDAOFactory getFactory(int type) {
        switch (type) {
            case DAO_FACTORY:
                return new fr.inria.anhalytics.ingest.dao.biblio.BiblioDAOFactory();
            //case MONGO_DAO_FACTORY:
            //    return new BiblioMongoDAOFactory();
            default:
                return null;
        }
    }
}
