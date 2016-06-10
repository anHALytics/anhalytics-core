package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.kb.entities.Publisher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class PublisherDAO extends DAO<Publisher, Long> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PublisherDAO.class);
    private static final String SQL_INSERT
            = "INSERT INTO PUBLISHER (name) VALUES (?)";

    private static final String SQL_SELECT_PUBLISHER_BY_NAME
            = "SELECT * FROM PUBLISHER WHERE name = ?";

    private static final String SQL_SELECT_PUBLISHER_BY_ID
            = "SELECT * FROM PUBLISHER WHERE publisherID = ?";

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
            statement.close();
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
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_PUBLISHER_BY_ID);
            preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, publisher_id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                publisher = new Publisher(
                        publisher_id,
                        rs.getString("name")
                );
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return publisher;
    }

    private Publisher findPublisherIfAlreadyStored(Publisher obj) throws SQLException {
        Publisher publisher = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_PUBLISHER_BY_NAME);
            preparedStatement.setString(1, obj.getName());
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                publisher = new Publisher(
                        rs.getLong("publisherID"),
                        rs.getString("name")
                );
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return publisher;

    }
}
