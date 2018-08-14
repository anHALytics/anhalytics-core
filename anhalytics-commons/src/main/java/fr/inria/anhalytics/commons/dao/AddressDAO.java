package fr.inria.anhalytics.commons.dao;

import fr.inria.anhalytics.commons.entities.Address;
import fr.inria.anhalytics.commons.entities.Country;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @author azhar
 */
public class AddressDAO extends DAO<Address, Long> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AddressDAO.class);

    private static final String SQL_INSERT
            = "INSERT INTO ADDRESS (addrLine, postBox, postCode, settlement, region, countryID) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_COUNTRY
            = "INSERT INTO COUNTRY (ISO) VALUES (?)";

    private static final String SQL_SELECT_LOCATIONSID_BY_ORGID
            = "SELECT addressID FROM LOCATION WHERE organisationID = ?";
    private static String SQL_SELECT_ADDR_BY_FIELDS
            = "SELECT * FROM ADDRESS addr WHERE addr.addrLine = ? AND addr.postBox = ? AND addr.postCode = ? AND addr.settlement = ? AND addr.region = ?";

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

        obj = createCountry(obj);

        Address foundObj = findAddressIfAlreadyStored(obj);
        if (foundObj != null) {
            obj.setAddressId(foundObj.getAddressId());
        } else {

            PreparedStatement statement = null;
            try {

                statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, obj.getAddrLine());
                statement.setString(2, obj.getPostBox());
                statement.setString(3, obj.getPostCode());
                statement.setString(4, obj.getSettlement());
                statement.setString(5, obj.getRegion());
                if (obj.getCountry() == null) {
                    statement.setNull(6, java.sql.Types.INTEGER);
                } else {
                    statement.setLong(6, obj.getCountry().getCountryID());
                }

                int code = statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    obj.setAddressId(rs.getLong(1));
                }

                result = true;
            } finally {
                closeQuietly(statement);
            }
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
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_ADDR_BY_ID);
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
                        new Country(result.getLong("COUNTRY.countryID"), result.getString("COUNTRY.ISO"))
                );
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            closeQuietly(preparedStatement);
        }
        return address;
    }

    public Country findCountry(String iso) {
        Country country = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_COUNTRY_BY_ISO);
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
            closeQuietly(preparedStatement);
        }
        return country;
    }

    public Address getOrganisationAddress(Long orgId) throws SQLException {

        Address address = null;
        PreparedStatement ps = null;
        try {
            ps = this.connect.prepareStatement(SQL_SELECT_LOCATIONSID_BY_ORGID);
            ps.setLong(1, orgId);
            // process the results
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                address = find(rs.getLong("addressID"));

            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            closeQuietly(ps);
        }
        return address;

    }

    private Address findAddressIfAlreadyStored(Address obj) throws SQLException {
        Address address = null;
        PreparedStatement ps;

        if (obj.getCountry() == null || obj.getCountry().getCountryID() == null) {
            ps = this.connect.prepareStatement(SQL_SELECT_ADDR_BY_FIELDS + "AND addr.countryID IS NULL");
        } else {
            ps = this.connect.prepareStatement(SQL_SELECT_ADDR_BY_FIELDS + "AND addr.countryID = ?");
        }

        try {
            ps.setString(1, obj.getAddrLine());
            ps.setString(2, obj.getPostBox());
            ps.setString(3, obj.getPostCode());
            ps.setString(4, obj.getSettlement());
            ps.setString(5, obj.getRegion());
            if (!(obj.getCountry() == null || obj.getCountry().getCountryID() == null)) {
                ps.setLong(6, obj.getCountry().getCountryID());
            }
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
                        obj.getCountry()
                );
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            closeQuietly(ps);
        }
        return address;
    }

    public Address createCountry(Address obj) throws SQLException{
        PreparedStatement statement1 = null;
        if (obj.getCountry() != null) {
            Country country = null;
            if (obj.getCountry().getIso() != null) {
                country = findCountry(obj.getCountry().getIso());
            }
            if (country == null) {
                try {
                    statement1 = connect.prepareStatement(SQL_INSERT_COUNTRY, Statement.RETURN_GENERATED_KEYS);
                    if (obj.getCountry().getIso() == null) {
                        statement1.setNull(1, java.sql.Types.VARCHAR);
                    } else {
                        logger.debug("Creating new country: with ISO: ", obj.getCountry().toString());
                        statement1.setString(1, obj.getCountry().getIso());
                    }
                    int code1 = statement1.executeUpdate();
                    ResultSet rs1 = statement1.getGeneratedKeys();

                    if (rs1.next()) {
                        obj.getCountry().setCountryID(rs1.getLong(1));
                    }

                } finally {
                    closeQuietly(statement1);
                }
            } else {
                obj.getCountry().setCountryID(country.getCountryID());
            }
        }
        return obj;
    }
}
