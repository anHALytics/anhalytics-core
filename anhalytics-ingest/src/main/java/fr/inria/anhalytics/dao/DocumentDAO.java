package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.ingest.entities.Document;
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
public class DocumentDAO extends DAO<Document> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDAO.class);

    private static final String SQL_INSERT
            = "INSERT INTO DOCUMENT (version, uri, TEImetadatas) VALUES (?, ?, ?)";
    private static final String READ_QUERY_DOCUMENTS
            = "SELECT * FROM DOCUMENT";

    private static final String SQL_SELECT_DOCID_BY_ORGID = "SELECT * FROM DOCUMENT_ORGANISATION WHERE organisationID = ?";

    public DocumentDAO(Connection conn) {
        super(conn);
    }
    private ResultSet rsDocuments = null;

    public boolean create(Document obj) throws SQLException {
        boolean result = false;
        if (obj.getDocID() != null) {
            throw new IllegalArgumentException("Document is already created, the document ID is not null.");
        }

        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, obj.getVersion());

        statement.setString(2, obj.getUri());
        statement.setString(3, obj.getTei_metadata());
        int code = statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();

        if (rs.next()) {
            obj.setDocID(rs.getLong(1));
        }

        result = true;
        return result;
    }

    public boolean delete(Document obj) {
        return false;
    }

    public boolean update(Document obj) {
        return false;
    }

    public Document find(Long doc_id) throws SQLException {
        Document document = new Document();

        ResultSet result = this.connect.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM document WHERE docID = " + doc_id);
        if (result.first()) {
            document = new Document(
                    doc_id,
                    result.getString("version"),
                    result.getString("TEImetadatas"
                    ),
                    result.getString("uri"
                    ));
        }
        return document;
    }

    public Document findByURI(String uri) {
        Document document = null;
        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM document WHERE uri = \"" + uri + "\"");
            if (result.first()) {
                document = new Document(
                        result.getLong("docID"),
                        result.getString("version"),
                        result.getString("TEImetadatas"
                        ),
                        result.getString("uri"
                        ));
            }

        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return document;
    }

    public boolean isMined(String uri) {
        boolean isMined = false;
        Document document = findByURI(uri);
        if (document != null) {
            isMined = true;
        }
        return isMined;
    }

    public boolean isCitationsMined(String uri) {
        return isMined(uri);
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
                                rs.getLong("docID"),
                                rs.getString("version"),
                                rs.getString("TEImetadatas"),
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
                documents.add(find(rs.getLong("docID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }
}
