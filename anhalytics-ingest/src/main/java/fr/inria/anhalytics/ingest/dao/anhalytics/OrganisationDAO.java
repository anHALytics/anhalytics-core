package fr.inria.anhalytics.ingest.dao.anhalytics;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.dao.DocumentDAO;
import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.ingest.entities.Organisation;
import fr.inria.anhalytics.ingest.entities.PART_OF;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class OrganisationDAO extends DAO<Organisation> {

    private static final String SQL_INSERT
            = "INSERT INTO ORGANISATION (type, url, structID) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_NAMES
            = "INSERT INTO ORGANISATION_NAME (organisationID, name) VALUES (?, ?)";
    private static final String SQL_INSERT_MOTHERS
            = "INSERT INTO PART_OF (organisation_motherID, organisationID, begin_date, end_date) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE_END_DATE
            = "UPDATE PART_OF SET end_date = ? WHERE organisationID = ? AND organisation_motherID = ?";

    private static final String SQL_SELECT_MOTHERID_BY_ORGID = "SELECT * FROM PART_OF WHERE organisationID = ?";

    private static final String SQL_SELECT_AUTHORSID_BY_DOCID = "SELECT personID FROM AUTHOR WHERE docID = ?";

    private static final String SQL_SELECT_ORGID_BY_PERSONID = "SELECT organisationID FROM AFFILIATION WHERE personID = ?";

    private static final String SQL_SELECT_ORG_BY_ID = "SELECT * FROM ORGANISATION org, ORGANISATION_NAME orgname WHERE org.organisationID = ? AND orgname.organisationID = ?";

    private static final String SQL_SELECTALL_ORG_BY_ID = "SELECT * FROM ORGANISATION org";

    private static final String READ_QUERY_ORG_BY_STRUCTID = "SELECT org.organisationID FROM ORGANISATION org WHERE org.structID = ? ";

    private static final String UPDATE_ORGANISATION = "UPDATE ORGANISATION SET type = ? ,structID = ? ,url = ? WHERE organisationID = ?";

    private static final String UPDATE_ORGANISATION_NAMES = "UPDATE ORGANISATION_NAME SET name = ? WHERE organisationID = ?";

    public OrganisationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Organisation obj) {

        boolean result = false;
        if (obj.getOrganisationId() != null) {
            throw new IllegalArgumentException("Organisation is already created, the Organisation ID is not null.");
        }

        Long orgId = getOrgEntityIfAlreadyStored(obj);
        if (orgId != null) {
            obj.setOrganisationId(orgId);
            update(obj);
        } else {
            PreparedStatement statement;
            PreparedStatement statement1;
            PreparedStatement statement2;

            try {
                statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, obj.getType());
                statement.setString(2, obj.getUrl());
                if (obj.getStructure() == null) {
                    statement.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    statement.setString(3, obj.getStructure());
                }
                int code = statement.executeUpdate();

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

                for (PART_OF rel : obj.getRels()) {

                    statement2 = connect.prepareStatement(SQL_INSERT_MOTHERS);
                    statement2.setLong(1, rel.getOrganisation_mother().getOrganisationId());
                    statement2.setLong(2, obj.getOrganisationId());
                    if (rel.getBeginDate() == null) {
                        statement2.setDate(3, new java.sql.Date(00000000L));
                    } else {
                        statement2.setDate(3, new java.sql.Date(rel.getBeginDate().getTime()));
                    }
                    if (rel.getEndDate() == null) {
                        statement2.setDate(4, new java.sql.Date(00000000L));
                    } else {
                        statement2.setDate(4, new java.sql.Date(rel.getEndDate().getTime()));
                    }

                    int code1 = statement2.executeUpdate();
                }

                result = true;
            } catch (SQLException ex) {
                Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    @Override
    public boolean delete(Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Organisation obj) {
        boolean result = false;
        try {

            PreparedStatement preparedStatement = this.connect.prepareStatement(UPDATE_ORGANISATION);
            preparedStatement.setString(1, obj.getType());
            preparedStatement.setString(2, obj.getStructure());
            preparedStatement.setString(3, obj.getUrl());
            preparedStatement.setLong(4, obj.getOrganisationId());
            int code1 = preparedStatement.executeUpdate();
            Organisation oldOrg = find(obj.getOrganisationId());
            Iterator<String> iter = obj.getNames().iterator();
            while (iter.hasNext()) {
                String str = iter.next();
                for (String str1 : oldOrg.getNames()) {
                    if (str.equals(str1)) {
                        iter.remove();
                        break;
                    }
                }
            }
            for (String orgName : obj.getNames()) {
                PreparedStatement statement1 = connect.prepareStatement(SQL_INSERT_NAMES);
                statement1.setLong(1, obj.getOrganisationId());
                statement1.setString(2, orgName);
                int code2 = statement1.executeUpdate();
            }

            //save date /
            Date pubDate = obj.getRels().size() > 0 ? obj.getRels().get(0).getBeginDate() : null;

            List<PART_OF> oldMothersOrg = findMothers(obj.getOrganisationId());
            Iterator<PART_OF> iter1 = oldMothersOrg.iterator();
            while (iter1.hasNext()) {
                PART_OF pof = iter1.next();
                Iterator<PART_OF> iter2 = obj.getRels().iterator();
                while (iter2.hasNext()) {
                    PART_OF pof1 = iter2.next();
                    if (pof1.getOrganisation_mother().getOrganisationId().equals(pof.getOrganisation_mother().getOrganisationId())) {
                        iter1.remove();
                        iter2.remove();
                        break;
                    }
                }
            }

            PreparedStatement statement2;
            for (PART_OF rel : obj.getRels()) {

                statement2 = connect.prepareStatement(SQL_INSERT_MOTHERS);
                statement2.setLong(1, rel.getOrganisation_mother().getOrganisationId());
                statement2.setLong(2, obj.getOrganisationId());
                if (rel.getBeginDate() == null) {
                    statement2.setDate(3, new java.sql.Date(00000000L));
                } else {
                    statement2.setDate(3, new java.sql.Date(rel.getBeginDate().getTime()));
                }
                if (rel.getEndDate() == null) {
                    statement2.setDate(4, new java.sql.Date(00000000L));
                } else {
                    statement2.setDate(4, new java.sql.Date(rel.getEndDate().getTime()));
                }

                int code2 = statement2.executeUpdate();
            }

            PreparedStatement statement3;
            for (PART_OF rel : oldMothersOrg) {

                statement3 = connect.prepareStatement(SQL_UPDATE_END_DATE);

                if (pubDate != null) {
                    statement3.setDate(1, new java.sql.Date(pubDate.getTime()));
                    statement3.setLong(2, obj.getOrganisationId());
                    statement3.setLong(3, rel.getOrganisation_mother().getOrganisationId());
                }

                int code3 = statement3.executeUpdate();
            }
            //update end_date if no relation is found yet
            result = true;
        } catch (MySQLIntegrityConstraintViolationException e) {
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Organisation find(Long id) {
        Organisation organisation = new Organisation();
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_ORG_BY_ID);
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                //try {
                organisation = new Organisation(
                        id,
                        rs.getString("org.type"),
                        rs.getString("org.url"),
                        rs.getString("org.structID"),
                        (new ArrayList<String>()),
                        findMothers(id)
                );
                organisation.getNames().add(rs.getString("orgname.name"));
                /*} catch (ParseException ex) {
                 Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                 }*/
            }
            while (rs.next()) {
                organisation.getNames().add(rs.getString("orgname.name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return organisation;
    }

    public List<PART_OF> findMothers(Long id) {
        List<PART_OF> rels = new ArrayList<PART_OF>();
        Organisation org = null;
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_MOTHERID_BY_ORGID);
            preparedStatement.setLong(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                org = find(rs.getLong("organisation_motherID"));
                try {
                    rels.add(new PART_OF(org, Utilities.parseStringDate(rs.getString("begin_date")), Utilities.parseStringDate(rs.getString("end_date"))));
                } catch (ParseException ex) {
                    Logger.getLogger(OrganisationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rels;
    }

    public List<Organisation> findAll() {
        List<Organisation> organisations = new ArrayList<Organisation>();
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECTALL_ORG_BY_ID);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                //try {
                organisations.add(new Organisation(
                        rs.getLong("org.organisationID"),
                        rs.getString("org.type"),
                        rs.getString("org.url"),
                        rs.getString("org.structID"),
                        new ArrayList<String>(),
                        findMothers(rs.getLong("org.organisationID"))
                ));
                /*} catch (ParseException ex) {
                 Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 */
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return organisations;
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
                    ps2.setLong(3, rs1.getLong("organisationID"));

                    ResultSet rs2 = ps2.executeQuery();
                    int size = 0;
                    if (rs2.first()) {
                        //try {
                        organisation = new Organisation(
                                rs2.getLong("org.organisationID"),
                                rs2.getString("org.type"),
                                rs2.getString("org.url"),
                                rs2.getString("org.structID"),
                                //rs2.getString("orgname.name")
                                new ArrayList<String>(),
                                findMothers(rs.getLong("org.organisationID"))
                        );
                        /*} catch (ParseException ex) {
                         Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                         */
                        organisation.addName(rs2.getString("orgname.name"));
                    }
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

    private Long getOrgEntityIfAlreadyStored(Organisation obj) {
        Long orgId = null;
        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(READ_QUERY_ORG_BY_STRUCTID);
            statement.setString(1, obj.getStructure());
            ResultSet rs = statement.executeQuery();
            if (rs.first()) {
                orgId = rs.getLong("org.organisationID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orgId;
    }
}
