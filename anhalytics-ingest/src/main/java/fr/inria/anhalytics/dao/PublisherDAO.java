package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.ingest.entities.Publisher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    public boolean create(Publisher obj) throws SQLException {
        boolean result = false;
        if (obj.getPublisherID() != null) {
            throw new IllegalArgumentException("Publisher is already created, the document ID is not null.");
        }
        Publisher foundObj = findPublisherIfAlreadyStored(obj);
        if (foundObj != null) {
            obj.setPublisherID(obj.getPublisherID());
        } else {
            PreparedStatement statement;
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getName());
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setPublisherID(rs.getLong(1));
            }

            result = true;
        }
        return result;
    }

    public boolean delete(Publisher obj) {
        return false;
    }

    public boolean update(Publisher obj) {
        return false;
    }

    public Publisher find(Long publisher_id) throws SQLException {
        Publisher publisher = new Publisher();

        ResultSet result = this.connect.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM publisher WHERE publisherID = " + publisher_id);
        if (result.first()) {
            publisher = new Publisher(
                    publisher_id,
                    result.getString("name")
            );
        }
        return publisher;
    }

    private Publisher findPublisherIfAlreadyStored(Publisher obj) throws SQLException {
        Publisher publisher = null;
        ResultSet result = this.connect.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM publisher WHERE name = \"" + obj.getName() + "\"");
        if (result.first()) {
            publisher = new Publisher(
                    result.getLong("publisherID"),
                    result.getString("name")
            );
        }
        return publisher;

    }
}
