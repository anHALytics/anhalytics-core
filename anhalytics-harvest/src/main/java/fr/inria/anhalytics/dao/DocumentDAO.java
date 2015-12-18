package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.entities.Document;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class DocumentDAO extends DAO<Document> {

    private static final String SQL_INSERT
            = "INSERT INTO DOCUMENT (version, uri, TEImetadatas) VALUES (?, ?, ?)";

    public DocumentDAO(Connection conn) {
        super(conn);
    }

    public boolean create(Document obj) {
        boolean result = false;
        if (obj.getDocID() != null) {
            throw new IllegalArgumentException("Document is already created, the document ID is not null.");
        }

        PreparedStatement statement;
        try {
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
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public boolean delete(Document obj) {
        return false;
    }

    public boolean update(Document obj) {
        return false;
    }

    public Document find(Long doc_id) {
        Document document = new Document();

        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return document;
    }
    
    public boolean isMined(String uri){
        Document document = findByURI(uri);
        if(document != null)
            return true;
        else
            return false;
    }
}
