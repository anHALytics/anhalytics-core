package fr.inria.anhalytics.dao;

import java.sql.Connection;

/**
 *
 * @author azhar
 */
public class DAOFactory extends AbstractDAOFactory{
  protected static final Connection conn = AnhalyticsConnection.getInstance();   

  public DAO getDocumentDAO(){
    return new DocumentDAO(conn);
  }

  public DAO getPublicationDAO(){
    return new PublicationDAO(conn);
  }

  public DAO getMonographDAO(){
    return new MonographDAO(conn);
  }

  public DAO getPublisherDAO(){
    return new PublisherDAO(conn);
  }   
}