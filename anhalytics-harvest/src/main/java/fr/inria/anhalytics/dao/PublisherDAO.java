package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.entities.Publisher;
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
public class PublisherDAO extends DAO<Publisher> {
    
    private static final String SQL_INSERT
            = "INSERT INTO PUBLISHER (name) VALUES (?)";

    public PublisherDAO(Connection conn) {
        super(conn);
    }

    public boolean create(Publisher obj) {
        boolean result = false;
        if (obj.getPublisherID() != null) {
            throw new IllegalArgumentException("Publisher is already created, the document ID is not null.");
        }

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getName());
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setPublisherID(rs.getLong(1));
            }
            
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public boolean delete(Publisher obj) {
        return false;
    }

    public boolean update(Publisher obj) {
        return false;
    }

    public Publisher find(Long publisher_id) {
        Publisher publisher = new Publisher();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM publisher WHERE publisherID = " + publisher_id);
            if (result.first()) {
                publisher = new Publisher(
                        publisher_id,
                        result.getString("name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return publisher;
    }
}
