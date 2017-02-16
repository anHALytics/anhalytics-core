package fr.inria.anhalytics.commons.dao.anhalytics;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.dao.DAO;
import fr.inria.anhalytics.commons.entities.Affiliation;
import fr.inria.anhalytics.commons.entities.Organisation;
import fr.inria.anhalytics.commons.entities.Person;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class AffiliationDAO extends DAO<Affiliation, Long> {
    
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AffiliationDAO.class);
    
    private static final String SQL_INSERT
            = "INSERT INTO AFFILIATION (organisationID, personID, from_date, until_date) VALUES (?, ?, ?, ?)";
    
    private static final String SQL_SELECT_AFF_BY_ID = "SELECT * FROM AFFILIATION WHERE affiliationID = ?";
    
    private static final String SQL_SELECT_AFF_BY_PERSONID_ORGID
            = "SELECT * FROM AFFILIATION WHERE personID = ? AND organisationID = ?";
    
    private static final String UPDATE_AFFILIATION = "UPDATE AFFILIATION SET from_date = ? ,until_date = ? WHERE affiliationID = ?";
    
    public AffiliationDAO(Connection conn) {
        super(conn);
    }
    
    private Affiliation getAffiliationIfAlreadyStored(Person pers, Organisation org) throws SQLException {
        Affiliation affiliation = null;
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement(SQL_SELECT_AFF_BY_PERSONID_ORGID);
            statement.setLong(1, pers.getPersonId());
            statement.setLong(2, org.getOrganisationId());
            ResultSet rs = statement.executeQuery();
            if (rs.first()) {
                try {
                    affiliation = new Affiliation(
                            rs.getLong("affiliationID"),
                            new ArrayList<Organisation>(),
                            pers,
                            Utilities.parseStringDate(rs.getString("from_date")),
                            Utilities.parseStringDate(rs.getString("until_date"))
                    );
                    affiliation.addOrganisation(org);
                    
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeQuietly(statement);
        }
        return affiliation;
    }
    
    @Override
    public boolean create(Affiliation obj) throws SQLException {
        boolean result = false;
        if (obj.getAffiliationId() != null) {
            throw new IllegalArgumentException("Affiliation is already created, the Affiliation ID is not null.");
        }
        
        PreparedStatement statement = null;
        
        for (Organisation org : obj.getOrganisations()) {
            
            Affiliation existingAff = getAffiliationIfAlreadyStored(obj.getPerson(), org);
            if (existingAff != null) {
                try {
                    statement = connect.prepareStatement(UPDATE_AFFILIATION, Statement.RETURN_GENERATED_KEYS);
                    if (obj.getFrom_date().before(existingAff.getFrom_date())) {
                        existingAff.setFrom_date(obj.getFrom_date());
                    } else if (obj.getUntil_date().after(existingAff.getUntil_date())) {
                        existingAff.setUntil_date(obj.getUntil_date());
                    }
                    statement.setDate(1, new java.sql.Date(existingAff.getFrom_date().getTime()));
                    statement.setDate(2, new java.sql.Date(existingAff.getUntil_date().getTime()));
                    statement.setLong(3, existingAff.getAffiliationId());
                    
                    int code = statement.executeUpdate();
                    result = true;
                } finally {
                    closeQuietly(statement);
                }
            } else {
                try {
                    statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                    statement.setLong(1, org.getOrganisationId());
                    statement.setLong(2, obj.getPerson().getPersonId());
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
                    ResultSet rs = statement.getGeneratedKeys();
                    
                    if (rs.next()) {
                        obj.setAffiliationId(rs.getLong(1));
                    }
                    
                    result = true;
                    
                } catch (MySQLIntegrityConstraintViolationException e) {
                    //e.printStackTrace();
                }finally{
                    closeQuietly(statement);
                }
            }
            
            
        }
        return result;
    }
    
    @Override
    public boolean delete(Affiliation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean update(Affiliation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Affiliation find(Long id) throws SQLException {
        Affiliation affiliation = new Affiliation();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_AFF_BY_ID);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                try {
                    affiliation = new Affiliation(
                            id,
                            new ArrayList<Organisation>(),
                            new Person(),
                            Utilities.parseStringDate(result.getString("begin_date")),
                            Utilities.parseStringDate(result.getString("until_date"))
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            closeQuietly(preparedStatement);
        }
        return affiliation;
    }
    
}
