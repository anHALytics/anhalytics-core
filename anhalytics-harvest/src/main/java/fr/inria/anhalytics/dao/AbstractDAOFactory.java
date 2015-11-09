package fr.inria.anhalytics.dao;

/**
 *
 * @author azhar
 */
public abstract class AbstractDAOFactory {

    public static final int DAO_FACTORY = 0;

    public abstract DAO getDocumentDAO();

    public abstract DAO getPublicationDAO();

    public abstract DAO getMonographDAO();

    public abstract DAO getPublisherDAO();

    public static AbstractDAOFactory getFactory(int type) {
        switch (type) {
            case DAO_FACTORY:
                return new DAOFactory();
            default:
                return null;
        }
    }
}
