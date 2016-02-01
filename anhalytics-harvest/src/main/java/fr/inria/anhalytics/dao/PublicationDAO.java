package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.entities.Document;
import fr.inria.anhalytics.entities.Monograph;
import fr.inria.anhalytics.entities.Publication;
import fr.inria.anhalytics.entities.Publisher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class PublicationDAO extends DAO<Publication> {

    private static final String SQL_INSERT
            = "INSERT INTO PUBLICATION (docID, monographID, publisherID, type, doc_title, date_printed, date_electronic, start_page, end_page, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public PublicationDAO(Connection conn) {
        super(conn);
    }

    public boolean create(Publication obj) {
        boolean result = false;
        if (obj.getPublicationID() != null) {
            throw new IllegalArgumentException("Publication is already created, the Publication ID is not null.");
        }

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, obj.getDocument().getDocID());
            statement.setLong(2, obj.getMonograph().getMonographID());
            if(obj.getPublisher().getPublisherID() == null)
                statement.setNull(3, java.sql.Types.INTEGER);
            else
                statement.setLong(3, obj.getPublisher().getPublisherID());
            
            statement.setString(4, obj.getType());
            
            statement.setString(5, obj.getDoc_title());
            
            if(obj.getDate_printed() == null)
                statement.setDate(6, new java.sql.Date(00000000L));
            else
                statement.setDate(6, new java.sql.Date(obj.getDate_printed().getTime()));
            
            statement.setString(7, obj.getDate_eletronic());
            
            statement.setString(8, obj.getStart_page());

            statement.setString(9, obj.getEnd_page());
            
            statement.setString(10, obj.getLanguage());
            
            
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setPublicationID(rs.getLong(1));
            }

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public boolean delete(Publication obj) {
        return false;
    }

    public boolean update(Publication obj) {
        return false;
    }

    public Publication find(Long publication_id) {
        Publication publication = new Publication();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM document ,monograph ,publisher, publication WHERE publicationID = " + publication_id + " AND document.docID = publication.docID" + " AND monograph.monographID = publication.monographID" + " AND publisher.publisherID = publication.publisherID");
            if (result.first()) {
                try {
                    publication = new Publication(
                            publication_id,
                            //result.getString("document.docID"),
                            new Document(),
                            new Monograph(),
                            new Publisher(),
                            result.getString("type"),
                            result.getString("doc_title"),
                            Utilities.parseStringDate(result.getString("date_printed")),
                            result.getString("date_eletronic"),
                            result.getString("start_page"),
                            result.getString("end_page"),
                            result.getString("language")
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(PublicationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return publication;
    }
}
