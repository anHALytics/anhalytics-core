package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.kb.entities.Document;
import fr.inria.anhalytics.kb.entities.Monograph;
import fr.inria.anhalytics.kb.entities.Publication;
import fr.inria.anhalytics.kb.entities.Publisher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class PublicationDAO extends DAO<Publication, Long> {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PublicationDAO.class);

    private static final String SQL_INSERT
            = "INSERT INTO PUBLICATION (docID, monographID, publisherID, type, doc_title, date_printed, date_electronic, start_page, end_page, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT
            = "SELECT * FROM DOCUMENT ,MONOGRAPH ,PUBLISHER, PUBLICATION WHERE publicationID = ? AND DOCUMENT.docID = PUBLICATION.docID AND MONOGRAPH.monographID = PUBLICATION.monographID AND PUBLISHER.publisherID = PUBLICATION.publisherID";

    private static final String SQL_SELECT_BY_DOCID
            = "SELECT * FROM DOCUMENT, PUBLICATION LEFT JOIN PUBLISHER ON PUBLISHER.publisherID = PUBLICATION.publisherID LEFT JOIN MONOGRAPH  ON MONOGRAPH.monographID = PUBLICATION.monographID WHERE PUBLICATION.docID = ? AND DOCUMENT.docID = ?";

    private static final String SQL_UPDATE
            = "UPDATE PUBLICATION SET type = ? ,doc_title = ? ,date_printed = ?,date_electronic = ?,start_page = ?,end_page = ? WHERE publicationID = ?";

    private static final String SQL_DELETE
            = "DELETE PUBLICATION WHERE publicationID = ?";

    public PublicationDAO(Connection conn) {
        super(conn);
    }

    public boolean create(Publication obj) throws SQLException {
        boolean result = false;
        if (obj.getPublicationID() != null) {
            throw new IllegalArgumentException("Publication is already created, the Publication ID is not null.");
        }

        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, obj.getDocument().getDocID());
        statement.setLong(2, obj.getMonograph().getMonographID());
        if (obj.getPublisher().getPublisherID() == null) {
            statement.setNull(3, java.sql.Types.INTEGER);
        } else {
            statement.setLong(3, obj.getPublisher().getPublisherID());
        }

        statement.setString(4, obj.getType());

        statement.setString(5, obj.getDoc_title());

        if (obj.getDate_printed() == null) {
            statement.setDate(6, new java.sql.Date(00000000L));
        } else {
            statement.setDate(6, new java.sql.Date(obj.getDate_printed().getTime()));
        }

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
        return result;
    }

    public boolean delete(Publication obj) throws SQLException {
        boolean result = false;
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_DELETE);
        preparedStatement.setLong(1, obj.getPublicationID());
        preparedStatement.executeUpdate();
        result = true;
        return result;
    }

    public boolean update(Publication obj) {
        return false;
    }

    public Publication find(Long publication_id) throws SQLException {
        Publication publication = new Publication();
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT);
        preparedStatement.setLong(1, publication_id);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.first()) {
            try {
                publication = new Publication(
                        publication_id,
                        new Document(rs.getString("docID"), rs.getString("version"), rs.getString("TEImetadatas"), rs.getString("URI")),
                        new Monograph(rs.getLong("monographID"), rs.getString("MONOGRAPH.type"), rs.getString("title"), rs.getString("shortname")),
                        new Publisher(rs.getLong("publisherID"), rs.getString("name")),
                        rs.getString("PUBLICATION.type"),
                        rs.getString("doc_title"),
                        Utilities.parseStringDate(rs.getString("date_printed")),
                        rs.getString("date_eletronic"),
                        rs.getString("start_page"),
                        rs.getString("end_page"),
                        rs.getString("language")
                );
            } catch (ParseException ex) {
                Logger.getLogger(PublicationDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return publication;
    }

    public List<Publication> findByDocId(String doc_id) {
        List<Publication> publications = new ArrayList<Publication>();
        try{
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_BY_DOCID);
        preparedStatement.setString(1, doc_id);
        preparedStatement.setString(2, doc_id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            try {
                publications.add(new Publication(
                        rs.getLong("PUBLICATION.publicationID"),
                        new Document(doc_id, rs.getString("version"), rs.getString("TEImetadatas"), rs.getString("URI")),
                        new Monograph(rs.getLong("monographID"), rs.getString("MONOGRAPH.type"), rs.getString("title"), rs.getString("shortname")),
                        new Publisher(rs.getLong("publisherID"), rs.getString("name")),
                        rs.getString("PUBLICATION.type"),
                        rs.getString("doc_title"),
                        Utilities.parseStringDate(rs.getString("date_printed")),
                        rs.getString("date_electronic"),
                        rs.getString("start_page"),
                        rs.getString("end_page"),
                        rs.getString("language")
                )
                );
            } catch (ParseException ex) {
                Logger.getLogger(PublicationDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        preparedStatement.close();
        rs.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return publications;
    }
}
