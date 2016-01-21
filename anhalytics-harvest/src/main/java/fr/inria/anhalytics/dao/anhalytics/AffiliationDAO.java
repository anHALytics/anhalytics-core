package fr.inria.anhalytics.dao.anhalytics;

import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.entities.Affiliation;
import fr.inria.anhalytics.entities.Organisation;
import fr.inria.anhalytics.entities.Person;
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

/**
 *
 * @author azhar
 */
public class AffiliationDAO extends DAO<Affiliation> {

    private static final String SQL_INSERT
            = "INSERT INTO AFFILIATION (organisationID, personID, begin_date, end_date) VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_AFFILIATION_BY_PERSONID = "SELECT * FROM AFFILIATION WHERE personID = ?";

    private static final String SQL_SELECT_ORG_BY_ID = "SELECT org.organisationID, org.type, org.url , org.description, orgname.name FROM ORGANISATION org, ORGANISATION_NAME orgname WHERE org.organisationID = ? AND orgname.organisationID = ?";

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
            for (Organisation org : obj.getOrganisations()) {
                statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, org.getOrganisationId());
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
            }
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
                            new ArrayList<Organisation>(),
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

    public List<Affiliation> getAffiliationByPersonID(Person person) {
        List<Affiliation> affiliations = new ArrayList<Affiliation>();

        Affiliation affiliation = null;

        try {

            Organisation organisation = null;
            PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_AFFILIATION_BY_PERSONID);
            ps.setLong(1, person.getPersonId());
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    affiliation = new Affiliation(
                            rs.getLong("AffiliationID"),
                            new ArrayList<Organisation>(),
                            person,
                            Utilities.parseStringDate(rs.getString("begin_date")),
                            Utilities.parseStringDate(rs.getString("end_date"))
                    );
                } catch (ParseException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
                PreparedStatement ps2 = this.connect.prepareStatement(SQL_SELECT_ORG_BY_ID);
                ps2.setLong(1, rs.getLong("organisationID"));
                ps2.setLong(2, rs.getLong("organisationID"));
                ResultSet rs2 = ps2.executeQuery();
                int size = 0;
                if (rs2.first()) {
                    organisation = new Organisation(
                            rs2.getLong("org.organisationID"),
                            rs2.getString("org.type"),
                            rs2.getString("org.url"),
                            rs2.getString("org.description"),
                            //rs2.getString("orgname.name")
                            new ArrayList<String>()
                    );
                    organisation.addName(rs2.getString("orgname.name"));
                    //System.out.print(rs2.getLong("org.organisationID") + " :  " + rs2.getString("org.STRUTUREcol"));
                }
                while (rs2.next()) {
                    organisation.addName(rs2.getString("orgname.name"));
                }
                affiliation.addOrganisation(organisation);
                affiliations.add(affiliation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return affiliations;
    }

}
