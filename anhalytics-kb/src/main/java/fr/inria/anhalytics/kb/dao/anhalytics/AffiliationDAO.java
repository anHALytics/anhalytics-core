package fr.inria.anhalytics.kb.dao.anhalytics;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.AbstractDAOFactory;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.kb.entities.Affiliation;
import fr.inria.anhalytics.kb.entities.Organisation;
import fr.inria.anhalytics.kb.entities.Person;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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
            = "INSERT INTO AFFILIATION (organisationID, personID, begin_date, end_date) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_AFF_BY_ID = "SELECT * FROM AFFILIATION WHERE affiliationID = ?";

    public AffiliationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Affiliation obj) throws SQLException {
        boolean result = false;
        if (obj.getAffiliationId() != null) {
            throw new IllegalArgumentException("Affiliation is already created, the Affiliation ID is not null.");
        }

        PreparedStatement statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);;
        for (Organisation org : obj.getOrganisations()) {
            statement.setLong(1, org.getOrganisationId());
            statement.setLong(2, obj.getPerson().getPersonId());
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
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setAffiliationId(rs.getLong(1));
            }

            result = true;
        }
        statement.close();
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
                            Utilities.parseStringDate(result.getString("end_date"))
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return affiliation;
    }

}
