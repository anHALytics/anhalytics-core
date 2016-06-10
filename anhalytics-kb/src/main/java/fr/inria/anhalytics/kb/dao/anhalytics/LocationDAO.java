package fr.inria.anhalytics.kb.dao.anhalytics;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.kb.entities.Address;
import fr.inria.anhalytics.kb.entities.Location;
import fr.inria.anhalytics.kb.entities.Organisation;
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

// check if location already exist and update dates if it is
        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
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
        statement.close();
        result = true;
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
            preparedStatement.close();
        }
        return addressId;
    }
}
