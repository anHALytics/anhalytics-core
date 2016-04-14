package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.kb.entities.Document;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class DocumentDAO extends DAO<Document, String> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDAO.class);

    private static final String SQL_INSERT
            = "INSERT INTO DOCUMENT (docID, version, uri) VALUES (?, ?, ?)";
    
    private static final String READ_QUERY_DOCUMENTS = "SELECT * FROM DOCUMENT";

    private static final String READ_QUERY_DOCID_BY_AUTHORS = "SELECT docID FROM AUTHOR WHERE personID = ?";

    private static final String SQL_SELECT_DOCID_BY_ORGID = "SELECT * FROM DOCUMENT_ORGANISATION WHERE organisationID = ?";

    public DocumentDAO(Connection conn) {
        super(conn);
    }

    public boolean create(Document obj) throws SQLException {
        boolean result = false;
        if (obj.getDocID() == null) {
            throw new IllegalArgumentException("The document ID is null, an ID should be provided.");
        }

        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT);
        statement.setString(1, obj.getDocID());
        statement.setString(2, obj.getVersion());

        statement.setString(3, obj.getUri());
        int code = statement.executeUpdate();

        result = true;
        return result;
    }

    public boolean delete(Document obj) {
        return false;
    }

    public boolean update(Document obj) {
        return false;
    }

    public Document find(String doc_id) {
        Document document = null;
        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM DOCUMENT WHERE docID = '" + doc_id + "'");
            if (result.first()) {
                document = new Document(
                        doc_id,
                        result.getString("version"),
                        result.getString("uri"
                        ));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return document;
    }

    public boolean isMined(String docId) {
        boolean isMined = false;
        Document document = find(docId);
        if (document != null) {
            isMined = true;
        }
        return isMined;
    }

    public boolean isCitationsMined(String docId) {
        return isMined(docId);
    }

    public List<Document> findAllDocuments() {
        List<Document> documents = new ArrayList<Document>();
        try {
            Statement statement = this.connect.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = statement.executeQuery(READ_QUERY_DOCUMENTS);

            while (rs.next()) {
                documents.add(
                        new Document(
                                rs.getString("docID"),
                                rs.getString("version"),
                                rs.getString("uri")
                        ));
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return documents;
    }

    public List<Document> getDocumentsByOrgId(Long organisationId) {
        List<Document> documents = new ArrayList<Document>();
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_DOCID_BY_ORGID);
            preparedStatement.setLong(1, organisationId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                documents.add(find(rs.getString("docID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }
    
    public List<Document> getDocumentsByAuthorId(Long personId) {
        List<Document> docs = new ArrayList<Document>();
        try {
            PreparedStatement ps = this.connect.prepareStatement(READ_QUERY_DOCID_BY_AUTHORS);
            ps.setLong(1, personId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                docs.add(find(rs.getString("docID")));
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return docs;
    }
}
