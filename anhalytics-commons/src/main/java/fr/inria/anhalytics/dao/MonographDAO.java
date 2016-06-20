package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.kb.entities.Monograph;
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
public class MonographDAO extends DAO<Monograph, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO MONOGRAPH (type, title, shortname) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_MONOGR_BY_ID
            = "SELECT * FROM MONOGRAPH WHERE monographID = ?";

    public MonographDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Monograph obj) {
        boolean result = false;
        if (obj.getMonographID() != null) {
            throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
        }

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getType());
            statement.setString(2, obj.getTitle());
            statement.setString(3, obj.getShortname());

            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setMonographID(rs.getLong(1));
            }
            statement.close();
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean delete(Monograph obj) {
        return false;
    }

    @Override
    public boolean update(Monograph obj) {
        return false;
    }

    @Override
    public Monograph find(Long monographID) throws SQLException {
        Monograph monograph = new Monograph();
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_MONOGR_BY_ID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, monographID);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                monograph = new Monograph(
                        monographID,
                        result.getString("type"),
                        result.getString("title"),
                        result.getString("shortname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            preparedStatement.close();
        }
        return monograph;
    }
}
