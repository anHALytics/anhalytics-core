package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.entities.Address;
import fr.inria.anhalytics.entities.Country;
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
public class AddressDAO extends DAO<Address> {

    private static final String SQL_INSERT
            = "INSERT INTO ADDRESS (addrLine, postBox, postCode, settlement, region, country, countryID) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_COUNTRY
            = "INSERT INTO COUNTRY (ISO) VALUES (?)";
    
    private static final String SQL_SELECT_LOCATIONSID_BY_ORGID
            = "SELECT addressID FROM LOCATION WHERE organisationID = ?";

    
    public AddressDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Address obj) {
        boolean result = false;
        if (obj.getAddressId() != null) {
            throw new IllegalArgumentException("Address is already created, the Address ID is not null.");
        }

        PreparedStatement statement;
        PreparedStatement statement1;
        try {
            if (obj.getCountry() != null) {

                statement1 = connect.prepareStatement(SQL_INSERT_COUNTRY, Statement.RETURN_GENERATED_KEYS);
                if (obj.getCountry().getIso() == null) {
                    statement1.setNull(1, java.sql.Types.VARCHAR);
                } else {
                    statement1.setString(1, obj.getCountry().getIso());
                }
                int code1 = statement1.executeUpdate();
                ResultSet rs1 = statement1.getGeneratedKeys();

                if (rs1.next()) {
                    obj.getCountry().setCountryID(rs1.getLong(1));
                }
            }
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getAddrLine());
            statement.setString(2, obj.getPostBox());
            statement.setString(3, obj.getPostCode());
            statement.setString(4, obj.getSettlement());
            statement.setString(5, obj.getRegion());
            statement.setString(6, obj.getCountryStr());
            if (obj.getCountry() == null) {
                statement.setNull(7, java.sql.Types.INTEGER);
            } else {
                statement.setLong(7, obj.getCountry().getCountryID());
            }

            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setAddressId(rs.getLong(1));
            }

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public boolean delete(Address obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Address obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Address find(Long id) {
        Address address = new Address();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM ADDRESS, COUNTRY WHERE addressID = " + id + " AND ADDRESS.countryID = COUNTRY.countryID");
            if (result.first()) {
                address = new Address(
                        id,
                        //result.getString("document.docID"),
                        result.getString("addrLine"),
                        result.getString("postBox"),
                        result.getString("postCode"),
                        result.getString("settlement"),
                        result.getString("region"),
                        result.getString("country"),
                        new Country(result.getLong("COUNTRY.countryID"), result.getString("COUNTRY.ISO"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return address;
    }
    
    public Address getOrganisationAddressById(Long orgId){

        Address address = null;

        try {
            PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_LOCATIONSID_BY_ORGID);
            ps.setLong(1, orgId);
            // process the results
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                address = find(rs.getLong("addressID"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return address;
    
    }
}
