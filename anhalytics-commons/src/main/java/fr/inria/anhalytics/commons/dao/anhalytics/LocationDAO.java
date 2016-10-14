package fr.inria.anhalytics.commons.dao.anhalytics;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.dao.DAO;
import fr.inria.anhalytics.commons.entities.Address;
import fr.inria.anhalytics.commons.entities.Location;
import fr.inria.anhalytics.commons.entities.Organisation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class LocationDAO extends DAO<Location, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO LOCATION (organisationID, addressID, begin_date, end_date) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_LOCATION_BY_ID
            = "SELECT * FROM LOCATION WHERE locationID = ? ";

    private static final String SQL_SELECT_LOCATION_BY_ORGID
            = "SELECT addressID FROM LOCATION WHERE organisationID = ?";

    public LocationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Location obj) throws SQLException {
        boolean result = false;
        if (obj.getLocationId() != null) {
            throw new IllegalArgumentException("Location is already created, the Location ID is not null.");
        }
        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
        try {
            //if (!isLocationExists(obj)) {

            statement.setLong(1, obj.getOrganisation().getOrganisationId());
            statement.setLong(2, obj.getAddress().getAddressId());

            if (obj.getBegin_date() == null) {
                statement.setDate(3, new java.sql.Date(00000000L));
            } else {
                statement.setDate(3, new java.sql.Date(obj.getBegin_date().getTime()));
            }

            if (obj.getEnd_date() == null) {
                statement.setDate(4, new java.sql.Date(00000000L));
            } else {
                statement.setDate(4, new java.sql.Date(obj.getEnd_date().getTime()));
            }
            int code = statement.executeUpdate();
            result = true;
        } catch (MySQLIntegrityConstraintViolationException e) {
            //e.printStackTrace();
        } finally {
            statement.close();
        }
        //}
        return result;
    }

    @Override
    public boolean delete(Location obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Location obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Location find(Long id) throws SQLException {
        Location location = new Location();
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_LOCATION_BY_ID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                try {
                    location = new Location(
                            id,
                            new Organisation(),
                            new Address(),
                            Utilities.parseStringDate(result.getString("begin_date")),
                            Utilities.parseStringDate(result.getString("end_date"))
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            preparedStatement.close();
        }
        return location;
    }

    public Long findAddressIdByOrganisationId(Long orgId) throws SQLException {
        Long addressId = null;
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_LOCATION_BY_ID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, orgId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                addressId = result.getLong("addressID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            preparedStatement.close();
        }
        return addressId;
    }
}
