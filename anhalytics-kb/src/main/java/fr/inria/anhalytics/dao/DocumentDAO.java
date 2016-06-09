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

    private static final String READ_QUERY_DOCID_BY_AUTHORS = "SELECT docID FROM AUTHORSHIP WHERE personID = ?";

    private static final String SQL_SELECT_DOCID_BY_ORGID = "SELECT * FROM DOCUMENT_ORGANISATION WHERE organisationID = ?";
    private static final String SQL_SELECT_DOC_BY_ID = "SELECT * FROM DOCUMENT WHERE docID = ?";

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
        statement.close();
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

            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_DOC_BY_ID);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setString(1, doc_id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                document = new Document(
                        doc_id,
                        rs.getString("version"),
                        rs.getString("uri"
                        ));
            }
            preparedStatement.close();
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
            PreparedStatement preparedStatement = this.connect.prepareStatement(READ_QUERY_DOCUMENTS);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                documents.add(
                        new Document(
                                rs.getString("docID"),
                                rs.getString("version"),
                                rs.getString("uri")
                        ));
            }
            preparedStatement.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return documents;
    }

    public List<Document> getDocumentsByOrgId(Long organisationId) {
        List<Document> documents = new ArrayList<Document>();
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_DOCID_BY_ORGID);
            preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, organisationId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                documents.add(find(rs.getString("docID")));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }

    public List<Document> getDocumentsByAuthorId(Long personId) {
        List<Document> docs = new ArrayList<Document>();
        try {
            PreparedStatement ps = this.connect.prepareStatement(READ_QUERY_DOCID_BY_AUTHORS);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setLong(1, personId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                docs.add(find(rs.getString("docID")));
            }
            ps.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return docs;
    }
}
