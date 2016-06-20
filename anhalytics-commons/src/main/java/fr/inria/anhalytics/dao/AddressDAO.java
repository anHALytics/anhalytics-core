package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.kb.entities.Address;
import fr.inria.anhalytics.kb.entities.Country;
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
public class AddressDAO extends DAO<Address, Long> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AddressDAO.class);

    private static final String SQL_INSERT
            = "INSERT INTO ADDRESS (addrLine, postBox, postCode, settlement, region, country, countryID) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_COUNTRY
            = "INSERT INTO COUNTRY (ISO) VALUES (?)";

    private static final String SQL_SELECT_LOCATIONSID_BY_ORGID
            = "SELECT addressID FROM LOCATION WHERE organisationID = ?";
    private static final String SQL_SELECT_ADDR_BY_FIELDS
            = "SELECT * FROM ADDRESS addr, COUNTRY country WHERE addr.addrLine = ? AND addr.postBox = ? AND addr.postCode = ? AND addr.settlement = ? AND addr.region = ? AND addr.country = ? AND addr.countryID = country.countryID";

    private static final String SQL_SELECT_ADDR_BY_ID
            = "SELECT * FROM ADDRESS, COUNTRY WHERE addressID = ? AND ADDRESS.countryID = COUNTRY.countryID";

    private static final String SQL_SELECT_COUNTRY_BY_ISO
            = "SELECT * FROM COUNTRY WHERE ISO = ?";

    public AddressDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Address obj) throws SQLException {
        boolean result = false;
        if (obj.getAddressId() != null) {
            throw new IllegalArgumentException("Address is already created, the Address ID is not null.");
        }
        Address foundObj = findAddressIfAlreadyStored(obj);
        if (foundObj != null) {
            obj.setAddressId(foundObj.getAddressId());
        } else {

            PreparedStatement statement;
            PreparedStatement statement1;
            if (obj.getCountry() != null) {
                Country country = null;
                if (obj.getCountry().getIso() != null) {
                    country = findCountry(obj.getCountry().getIso());
                }
                if (country == null) {
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
                    statement1.close();
                } else {
                    obj.getCountry().setCountryID(country.getCountryID());
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
            statement.close();
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
    public Address find(Long id) throws SQLException {
        Address address = new Address();
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_ADDR_BY_ID);
        try {
            preparedStatement.setLong(1, id);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            ResultSet result = preparedStatement.executeQuery();
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
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return address;
    }

    public Country findCountry(String iso) throws SQLException {
        Country country = null;
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_COUNTRY_BY_ISO);
        try {
            preparedStatement.setString(1, iso);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                country = new Country(
                        result.getLong("countryID"),
                        //result.getString("document.docID"),
                        iso
                );
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return country;
    }

    public Address getOrganisationAddress(Long orgId) throws SQLException {

        Address address = null;
        PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_LOCATIONSID_BY_ORGID);
        try {
            ps.setLong(1, orgId);
            // process the results
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                address = find(rs.getLong("addressID"));

            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            ps.close();
        }
        return address;

    }

    private Address findAddressIfAlreadyStored(Address obj) throws SQLException {
        Address address = null;
        PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_ADDR_BY_FIELDS);
        try {
            ps.setString(1, obj.getAddrLine());
            ps.setString(2, obj.getPostBox());
            ps.setString(3, obj.getPostCode());
            ps.setString(4, obj.getSettlement());
            ps.setString(5, obj.getRegion());
            ps.setString(6, obj.getCountryStr());
            // process the results
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                address = new Address(
                        rs.getLong("addr.addressID"),
                        rs.getString("addr.addrLine"),
                        rs.getString("addr.postBox"),
                        rs.getString("addr.postCode"),
                        rs.getString("addr.settlement"),
                        rs.getString("addr.region"),
                        rs.getString("addr.country"),
                        new Country(rs.getLong("country.countryID"), rs.getString("COUNTRY.ISO"))
                );
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            ps.close();
        }
        return address;
    }
}
