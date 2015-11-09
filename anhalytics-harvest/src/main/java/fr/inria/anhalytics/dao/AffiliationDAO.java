package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.entities.Affiliation;
import fr.inria.anhalytics.entities.Organisation;
import fr.inria.anhalytics.entities.Person;
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
public class AffiliationDAO extends DAO<Affiliation> {

    private static final String SQL_INSERT
            = "INSERT INTO AFFILIATION (organisationID, personID, begin_date, end_date) VALUES (?, ?, ?, ?)";

    public AffiliationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Affiliation obj) {
        boolean result = false;
        if (obj.getAffiliationId() != null) {
            throw new IllegalArgumentException("Affiliation is already created, the Affiliation ID is not null.");
        }

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, obj.getOrganisation().getOrganisationId());
            statement.setLong(2, obj.getPerson().getPersonId());
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
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setAffiliationId(rs.getLong(1));
            }

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
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
    public Affiliation find(Long id) {
        Affiliation affiliation = new Affiliation();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM affiliation WHERE affiliationID = " + id);
            if (result.first()) {
                try {
                    affiliation = new Affiliation(
                            id,
                            new Organisation(),
                            new Person(),
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
        return affiliation;
    }

}
