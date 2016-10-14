package fr.inria.anhalytics.commons.dao;

import fr.inria.anhalytics.commons.entities.Document;
import fr.inria.anhalytics.commons.entities.Document_Identifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static final String SQL_INSERT_IDENTIFIER
            = "INSERT INTO DOCUMENT_IDENTIFIER (docID, ID, Type) VALUES (?, ?, ?)";

    private static final String READ_QUERY_DOCUMENTS = "SELECT * FROM DOCUMENT";

    private static final String READ_QUERY_DOCID_BY_AUTHORS = "SELECT docID FROM AUTHORSHIP WHERE personID = ?";

    private static final String SQL_SELECT_DOCID_BY_ORGID = "SELECT * FROM DOCUMENT_ORGANISATION WHERE organisationID = ?";
    private static final String SQL_SELECT_DOC_BY_ID = "SELECT * FROM DOCUMENT WHERE docID = ?";

    public DocumentDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Document obj) throws SQLException {
        boolean result = false;
        if (obj.getDocID() == null) {
            throw new IllegalArgumentException("The document ID is null, an ID should be provided.");
        }

        PreparedStatement statement, statement1;
        statement = connect.prepareStatement(SQL_INSERT);
        statement.setString(1, obj.getDocID());
        statement.setString(2, obj.getVersion());

        statement.setString(3, obj.getUri());
        int code = statement.executeUpdate();
        statement.close();

        statement1 = connect.prepareStatement(SQL_INSERT_IDENTIFIER);
        for (Document_Identifier di : obj.getDocument_Identifiers()) {
            try {
                statement1.setString(1, obj.getDocID());
                statement1.setString(2, di.getId());
                statement1.setString(3, di.getType());

                int code3 = statement1.executeUpdate();
            } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
            }
        }
        statement1.close();

        result = true;
        return result;
    }

    @Override
    public boolean delete(Document obj) {
        return false;
    }

    @Override
    public boolean update(Document obj) {
        return false;
    }

    @Override
    public Document find(String doc_id) throws SQLException {
        Document document = null;
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_DOC_BY_ID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setString(1, doc_id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                document = new Document(
                        doc_id,
                        rs.getString("version"),
                        rs.getString("uri"
                        ),
                new ArrayList<Document_Identifier>());
            }
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            preparedStatement.close();
        }
        return document;
    }

    public boolean isMined(String docId) throws SQLException {
        boolean isMined = false;
        Document document = find(docId);
        if (document != null) {
            isMined = true;
        }
        return isMined;
    }

    public boolean isCitationsMined(String docId) throws SQLException {
        return isMined(docId);
    }

    public List<Document> findAllDocuments() throws SQLException {
        List<Document> documents = new ArrayList<Document>();
        PreparedStatement preparedStatement = this.connect.prepareStatement(READ_QUERY_DOCUMENTS);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                documents.add(
                        new Document(
                                rs.getString("docID"),
                                rs.getString("version"),
                                rs.getString("uri"),
                new ArrayList<Document_Identifier>()
                        ));
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return documents;
    }

    public List<Document> getDocumentsByOrgId(Long organisationId) throws SQLException {
        List<Document> documents = new ArrayList<Document>();
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_DOCID_BY_ORGID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, organisationId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                documents.add(find(rs.getString("docID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            preparedStatement.close();
        }
        return documents;
    }

    public List<Document> getDocumentsByAuthorId(Long personId) throws SQLException {
        List<Document> docs = new ArrayList<Document>();
        PreparedStatement ps = this.connect.prepareStatement(READ_QUERY_DOCID_BY_AUTHORS);
        try {
            //ps.setFetchSize(Integer.MIN_VALUE);
            ps.setLong(1, personId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                docs.add(find(rs.getString("docID")));
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            ps.close();
        }
        return docs;
    }
}
