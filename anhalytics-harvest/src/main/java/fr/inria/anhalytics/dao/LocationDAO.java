package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.entities.Address;
import fr.inria.anhalytics.entities.Location;
import fr.inria.anhalytics.entities.Organisation;
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
public class LocationDAO extends DAO<Location> {

    private static final String SQL_INSERT
            = "INSERT INTO LOCATION (organisationID, addressID, begin_date, end_date) VALUES (?, ?, ?, ?)";

    public LocationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Location obj) {
        boolean result = false;
        if (obj.getLocationId() != null) {
            throw new IllegalArgumentException("Location is already created, the Location ID is not null.");
        }

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, obj.getOrganisation().getOrganisationId());
            statement.setLong(2, obj.getAddress().getAddressId());

            if (obj.getBegin_date() == null) {
                statement.setNull(3, java.sql.Types.DATE);
            } else {
                statement.setDate(3, new java.sql.Date(obj.getBegin_date().getTime()));
            }

            if (obj.getEnd_date() == null) {
                statement.setNull(4, java.sql.Types.DATE);
            } else {
                statement.setDate(4, new java.sql.Date(obj.getEnd_date().getTime()));
            }
            int code = statement.executeUpdate();

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public Location find(Long id) {
        Location location = new Location();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM location WHERE locationID = " + id);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return location;
    }

}
