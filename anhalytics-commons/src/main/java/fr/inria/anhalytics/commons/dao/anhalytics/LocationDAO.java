package fr.inria.anhalytics.commons.dao.anhalytics;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.dao.DAO;
import fr.inria.anhalytics.commons.entities.Address;
import fr.inria.anhalytics.commons.entities.Location;
import fr.inria.anhalytics.commons.entities.Organisation;
import fr.inria.anhalytics.commons.utilities.Utilities;

import java.sql.*;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author azhar
 */
public class LocationDAO extends DAO<Location, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO LOCATION (organisationID, addressID, from_date, until_date) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_LOCATION_BY_ID
            = "SELECT * FROM LOCATION WHERE locationID = ? ";

    private static final String SQL_SELECT_LOCATION_BY_orgID_addrID
            = "SELECT * FROM LOCATION WHERE organisationID = ? AND addressID = ?";

    private static final String SQL_SELECT_LOCATION_BY_ORGID
            = "SELECT addressID FROM LOCATION WHERE organisationID = ?";

    private static final String UPDATE_LOCATION = "UPDATE LOCATION SET from_date = ? ,until_date = ? WHERE locationID = ?";

    public LocationDAO(Connection conn) {
        super(conn);
    }

    private Location getLocationIfAlreadyStored(Location obj) throws SQLException {
        Location location = null;
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement(SQL_SELECT_LOCATION_BY_orgID_addrID);
            statement.setLong(1, obj.getOrganisation().getOrganisationId());
            statement.setLong(2, obj.getAddress().getAddressId());
            ResultSet rs = statement.executeQuery();
            if (rs.first()) {
                try {
                    location = new Location(
                            rs.getLong("locationID"),
                            obj.getOrganisation(),
                            obj.getAddress(),
                            Utilities.parseStringDate(rs.getString("from_date")),
                            Utilities.parseStringDate(rs.getString("until_date"))
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(statement);
        }
        return location;
    }

    @Override
    public boolean create(Location obj) throws SQLException {
        boolean result = false;
        if (obj.getLocationId() != null) {
            throw new IllegalArgumentException("Location is already created, the Location ID is not null.");
        }

        PreparedStatement statement = null;
        Location existingLocation = getLocationIfAlreadyStored(obj);
        if (existingLocation != null) {
            try{
            statement = connect.prepareStatement(UPDATE_LOCATION, Statement.RETURN_GENERATED_KEYS);
            if (obj.getFrom_date().before(existingLocation.getFrom_date())) {
                existingLocation.setFrom_date(obj.getFrom_date());
            } else if (obj.getUntil_date().after(existingLocation.getUntil_date())) {
                existingLocation.setUntil_date(obj.getUntil_date());
            }
            statement.setDate(1, new java.sql.Date(existingLocation.getFrom_date().getTime()));
            statement.setDate(2, new java.sql.Date(existingLocation.getUntil_date().getTime()));
            statement.setLong(3, existingLocation.getLocationId());
            int code = statement.executeUpdate();
            result = true;
            }finally{
                closeQuietly(statement);
            }
        } else {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            try {
                //if (!isLocationExists(obj)) {

                statement.setLong(1, obj.getOrganisation().getOrganisationId());
                statement.setLong(2, obj.getAddress().getAddressId());

                if (obj.getFrom_date() == null) {
                    statement.setDate(3, new java.sql.Date(00000000L));
                } else {
                    statement.setDate(3, new java.sql.Date(obj.getFrom_date().getTime()));
                }

                if (obj.getUntil_date() == null) {
                    statement.setDate(4, new java.sql.Date(00000000L));
                } else {
                    statement.setDate(4, new java.sql.Date(obj.getUntil_date().getTime()));
                }
                int code = statement.executeUpdate();
                result = true;
            } catch (MySQLIntegrityConstraintViolationException e) {
                //e.printStackTrace();
            } finally {
                closeQuietly(statement);
            }
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
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_LOCATION_BY_ID);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                try {
                    location = new Location(
                            id,
                            new Organisation(),
                            new Address(),
                            Utilities.parseStringDate(result.getString("from_date")),
                            Utilities.parseStringDate(result.getString("until_date"))
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
        }
        return location;
    }

    public Long findAddressIdByOrganisationId(Long orgId) throws SQLException {
        Long addressId = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_LOCATION_BY_ID);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, orgId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                addressId = result.getLong("addressID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(preparedStatement);
        }
        return addressId;
    }
}
