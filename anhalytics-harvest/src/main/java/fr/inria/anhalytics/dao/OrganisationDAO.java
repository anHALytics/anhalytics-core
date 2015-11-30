package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.entities.Organisation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class OrganisationDAO extends DAO<Organisation> {

    private static final String SQL_INSERT
            = "INSERT INTO ORGANISATION (type, url, description) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_NAMES
            = "INSERT INTO ORGANISATION_NAME (organisationID, name) VALUES (?, ?)";

    private static final String SQL_SELECT_AUTHORSID_BY_DOCID = "SELECT personID FROM AUTHOR WHERE docID = ?";

    private static final String SQL_SELECT_ORGID_BY_PERSONID = "SELECT organisationID FROM AFFILIATION WHERE personID = ?";

    private static final String SQL_SELECT_ORG_BY_ID = "SELECT org.organisationID, org.type, org.url , org.description, orgname.name FROM ORGANISATION org, ORGANISATION_NAME orgname WHERE org.organisationID = ? AND orgname.organisationID = ?";

    public OrganisationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Organisation obj) {

        boolean result = false;
        if (obj.getOrganisationId() != null) {
            throw new IllegalArgumentException("Organisation is already created, the Organisation ID is not null.");
        }

        PreparedStatement statement;
        PreparedStatement statement1;
        
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getType());
            statement.setString(2, obj.getUrl());
            if (obj.getStructure() == null) {
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(3, obj.getStructure());
            }
            int code  = statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setOrganisationId(rs.getLong(1));
            }

            for (String name : obj.getNames()) {

                statement1 = connect.prepareStatement(SQL_INSERT_NAMES);
                statement1.setLong(1, obj.getOrganisationId());
                statement1.setString(2, name);
                int code1 = statement1.executeUpdate();
            }

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public boolean delete(Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Organisation find(Long id) {
        Organisation organisation = new Organisation();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM organisation, organisation_name WHERE organisationID = " + id + "AND organisation_name.organisationID = " + id);
            if (result.first()) {
                organisation = new Organisation(
                        id,
                        result.getString("type"),
                        result.getString("url"),
                        result.getString("structure"),
                        //result.getString("organisation_name.name")
                        new ArrayList<String>()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return organisation;
    }

    

    public List<Organisation> getOrganisationsByDocId(Long docId) {
        List<Organisation> orgs = new ArrayList<Organisation>();

        Organisation organisation = null;

        try {
            PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_AUTHORSID_BY_DOCID);
            ps.setLong(1, docId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                PreparedStatement ps1 = this.connect.prepareStatement(SQL_SELECT_ORGID_BY_PERSONID);
                ps1.setLong(1, rs.getLong("personID"));
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    PreparedStatement ps2 = this.connect.prepareStatement(SQL_SELECT_ORG_BY_ID);
                    ps2.setLong(1, rs1.getLong("organisationID"));
                    ps2.setLong(2, rs1.getLong("organisationID"));
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
                        organisation.addName(rs2.getString("orgname.name"));}
                    while (rs2.next()) {
                        organisation.addName(rs2.getString("orgname.name"));
                    }
                    orgs.add(organisation);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orgs;
    }
}
