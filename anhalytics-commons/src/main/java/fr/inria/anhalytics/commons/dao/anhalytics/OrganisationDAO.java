package fr.inria.anhalytics.commons.dao.anhalytics;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.dao.DAO;
import fr.inria.anhalytics.commons.dao.In_SerialDAO;
import fr.inria.anhalytics.commons.entities.*;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author azhar
 */
public class OrganisationDAO extends DAO<Organisation, Long> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(OrganisationDAO.class);

    private static final String SQL_INSERT
            = "INSERT INTO ORGANISATION (type, url, status) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_NAMES
            = "INSERT INTO ORGANISATION_NAME (organisationID, name, lastupdate_date) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_MOTHERS
            = "INSERT INTO PART_OF (organisation_motherID, organisationID, from_date, until_date) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_IDENTIFIERS
            = "INSERT INTO ORGANISATION_IDENTIFIER (organisationID, ID, type) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE_END_DATE
            = "UPDATE PART_OF SET until_date = ? WHERE organisationID = ? AND organisation_motherID = ?";

    private static final String SQL_SELECT_AFFILIATION_BY_PERSONID = "SELECT * FROM AFFILIATION WHERE personID = ?";

    private static final String SQL_SELECT_ORGNAME_BY_ORGID = "SELECT * FROM ORGANISATION_NAME WHERE organisationID = ?";

    private static final String SQL_SELECT_AUTHORSID_BY_DOCID = "SELECT personID FROM AUTHOR WHERE docID = ?";

    private static final String SQL_SELECT_ORGID_BY_PERSONID = "SELECT organisationID FROM AFFILIATION WHERE personID = ?";

    private static final String SQL_SELECT_ORG_BY_ID = "SELECT * FROM ORGANISATION org WHERE org.organisationID = ? ";

    private static final String SQL_SELECT_ORGNAMES_BY_ID = "SELECT * FROM ORGANISATION_NAME orgname WHERE orgname.organisationID = ?";

    private static final String SQL_SELECT_ORGIDENTIFIERS_BY_ID = "SELECT * FROM ORGANISATION_IDENTIFIER org_id WHERE org_id.organisationID = ?";

    private static final String SQL_SELECT_ORGPARENTS_BY_ID = "SELECT * FROM PART_OF part_of WHERE part_of.organisationID = ?";

    private static final String SQL_SELECTALL = "SELECT * FROM ORGANISATION org";

    private static final String READ_QUERY_ORG_BY_IDENTIFIER = "SELECT org_id.organisationID FROM ORGANISATION_IDENTIFIER org_id WHERE org_id.ID = ? AND  org_id.Type = ?";

    private static final String READ_QUERY_ORG_BY_NAME = "SELECT organisationID FROM ORGANISATION_NAME WHERE name = ?";

    private static final String UPDATE_ORGANISATION = "UPDATE ORGANISATION SET type = ? , url = ?, status = ? WHERE organisationID = ?";

    private static final String UPDATE_ORGANISATION_NAME = "UPDATE ORGANISATION_NAME SET lastupdate_date = ? WHERE organisation_nameID = ?";

    private static final String SQL_SELECT_ORGANISATIONS_BY_DOCUMENT
            = "SELECT * from DOCUMENT_ORGANISATION WHERE docID = ?";

    private static final String SQL_SELECT_PARTOF_BY_orgID_motherorgID
            = "SELECT * FROM PART_OF WHERE organisationID = ? AND organisation_motherID = ?";

    private static final String UPDATE_PART_OF = "UPDATE PART_OF SET from_date = ? ,until_date = ? WHERE organisationID = ? AND organisation_motherID = ?";

    public OrganisationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Organisation obj) throws SQLException {

        boolean result = false;
        if (obj.getOrganisationId() != null) {
            throw new IllegalArgumentException("Organisation is already created, the Organisation ID is not null.");
        }

        Long orgId = getOrgEntityIfAlreadyStored(obj);
        if (orgId != null) {
            obj.setOrganisationId(orgId);
            update(obj);
        } else {
            PreparedStatement statement = null;
            PreparedStatement statement1 = null;
            PreparedStatement statement2 = null;
            PreparedStatement statement3 = null;

            try {
                statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, obj.getType());
                statement.setString(2, obj.getUrl());
                statement.setString(3, obj.getStatus());
                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    obj.setOrganisationId(rs.getLong(1));
                }

                statement1 = connect.prepareStatement(SQL_INSERT_NAMES);

                for (Organisation_Name name : obj.getNames()) {
                    try {
                        statement1.setLong(1, obj.getOrganisationId());
                        statement1.setString(2, name.getName());
                        if (name.getLastupdate_date() == null) {
                            statement1.setDate(3, new java.sql.Date(00000000L));
                        } else {
                            statement1.setDate(3, new java.sql.Date(name.getLastupdate_date().getTime()));
                        }
                        int code1 = statement1.executeUpdate();
                    } catch (MySQLIntegrityConstraintViolationException e) {
                        //LOGGER.error("Error: ", e);
                    }
                }

                statement2 = connect.prepareStatement(SQL_INSERT_MOTHERS);
                for (PART_OF rel : obj.getRels()) {
                    try {
                        //avoid stackoverflow infinit loops!
                        PART_OF existingOppositepart_of = getPartOfIfAlreadyStored(rel.getOrganisation_mother(), obj);
                        if (existingOppositepart_of == null) {
                            statement2.setLong(1, rel.getOrganisation_mother().getOrganisationId());
                            statement2.setLong(2, obj.getOrganisationId());
                            if (rel.getFromDate() == null) {
                                statement2.setDate(3, new java.sql.Date(00000000L));
                            } else {
                                statement2.setDate(3, new java.sql.Date(rel.getFromDate().getTime()));
                            }
                            if (rel.getUntilDate() == null) {
                                statement2.setDate(4, new java.sql.Date(00000000L));
                            } else {
                                statement2.setDate(4, new java.sql.Date(rel.getUntilDate().getTime()));
                            }
                            int code1 = statement2.executeUpdate();
                        }
                    } catch (MySQLIntegrityConstraintViolationException e) {
                        //LOGGER.error("Error: ", e);
                    }
                }

                statement3 = connect.prepareStatement(SQL_INSERT_IDENTIFIERS);
                for (Organisation_Identifier oi : obj.getOrganisation_identifiers()) {
                    try {
                        statement3.setLong(1, obj.getOrganisationId());
                        statement3.setString(2, oi.getId());
                        statement3.setString(3, oi.getType());

                        int code3 = statement3.executeUpdate();
                    } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
                    }
                }
                result = true;
            } finally {
                closeQuietly(statement);
                closeQuietly(statement1);
                closeQuietly(statement2);
                closeQuietly(statement3);
            }

        }
        return result;
    }

    @Override
    public boolean delete(Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Organisation obj) throws SQLException {
        boolean result = false;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement1 = null;
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;

        try {
            preparedStatement = this.connect.prepareStatement(UPDATE_ORGANISATION);
            preparedStatement1 = this.connect.prepareStatement(UPDATE_ORGANISATION_NAME);
            statement1 = connect.prepareStatement(SQL_INSERT_NAMES);
            statement1.setFetchSize(Integer.MIN_VALUE);

            statement3 = connect.prepareStatement(SQL_UPDATE_END_DATE);
            statement3.setFetchSize(Integer.MIN_VALUE);

            preparedStatement.setString(1, obj.getType());
            preparedStatement.setString(2, obj.getUrl());
            preparedStatement.setString(3, obj.getStatus());
            preparedStatement.setLong(4, obj.getOrganisationId());
            int code1 = preparedStatement.executeUpdate();

            List<Organisation_Name> names = getOrganisationNames(obj.getOrganisationId());

            for (Organisation_Name name : obj.getNames()) {
                if (names.contains(name)) {
                    Organisation_Name existingName = names.get(names.indexOf(name));
                    if (name.getLastupdate_date().after(existingName.getLastupdate_date())) {
                        // we keep the most recent publication date to keep track of changing names !
                        try {
                            if (name.getLastupdate_date() == null) {
                                preparedStatement1.setDate(1, new java.sql.Date(00000000L));
                            } else {
                                preparedStatement1.setDate(1, new java.sql.Date(name.getLastupdate_date().getTime()));
                            }
                            preparedStatement1.setLong(2, existingName.getOrganisation_nameid());
                            int code2 = preparedStatement1.executeUpdate();
                        } catch (MySQLIntegrityConstraintViolationException e) {
                            //LOGGER.error("Error: ", e);
                        }
                    }
                    //update pub date
                } else {
                    try {
                        statement1.setLong(1, obj.getOrganisationId());
                        statement1.setString(2, name.getName());
                        if (name.getLastupdate_date() == null) {
                            statement1.setDate(3, new java.sql.Date(00000000L));
                        } else {
                            statement1.setDate(3, new java.sql.Date(name.getLastupdate_date().getTime()));
                        }
                        int code2 = statement1.executeUpdate();
                    } catch (MySQLIntegrityConstraintViolationException e) {
                        //LOGGER.error("Error: ", e);
                    }
                }
            }

            //}
            //save date /
            if (obj.getRels().size() > 0) {
                for (PART_OF rel : obj.getRels()) {

                    try {

                        PART_OF existingpart_of = getPartOfIfAlreadyStored(obj, rel.getOrganisation_mother());
                        if (existingpart_of != null) {

                            statement2 = connect.prepareStatement(UPDATE_PART_OF, Statement.RETURN_GENERATED_KEYS);
                            if (rel.getFromDate().before(existingpart_of.getFromDate())) {
                                existingpart_of.setFromDate(rel.getFromDate());
                            } else if (rel.getUntilDate().after(existingpart_of.getUntilDate())) {
                                existingpart_of.setUntilDate(rel.getUntilDate());
                            }
                            statement2.setDate(1, new java.sql.Date(existingpart_of.getFromDate().getTime()));

                            statement2.setDate(2, new java.sql.Date(existingpart_of.getUntilDate().getTime()));
                            statement2.setLong(3, obj.getOrganisationId());
                            statement2.setLong(4, rel.getOrganisation_mother().getOrganisationId());
                            int code = statement2.executeUpdate();
                            result = true;
                            statement2.close();
                        } else {

                            PART_OF existingInversepart_of = getPartOfIfAlreadyStored(rel.getOrganisation_mother(), obj);
                            if (existingInversepart_of == null) {
                                statement2 = connect.prepareStatement(SQL_INSERT_MOTHERS);

                                statement2.setFetchSize(Integer.MIN_VALUE);
                                statement2.setLong(1, rel.getOrganisation_mother().getOrganisationId());
                                statement2.setLong(2, obj.getOrganisationId());
                                if (rel.getFromDate() == null) {
                                    statement2.setDate(3, new java.sql.Date(00000000L));
                                } else {
                                    statement2.setDate(3, new java.sql.Date(rel.getFromDate().getTime()));
                                }
                                if (rel.getUntilDate() == null) {
                                    statement2.setDate(4, new java.sql.Date(00000000L));
                                } else {
                                    statement2.setDate(4, new java.sql.Date(rel.getUntilDate().getTime()));
                                }

                                int code2 = statement2.executeUpdate();
                                statement2.close();
                            }
                        }
                    } catch (MySQLIntegrityConstraintViolationException e) {
                        //LOGGER.error("Error: ", e);
                    }
                }
                //update end_date if no relation is found yet
                result = true;

            }
            statement3 = connect.prepareStatement(SQL_INSERT_IDENTIFIERS);
            for (Organisation_Identifier oi : obj.getOrganisation_identifiers()) {
                try {
                    statement3.setLong(1, obj.getOrganisationId());
                    statement3.setString(2, oi.getId());
                    statement3.setString(3, oi.getType());

                    int code3 = statement3.executeUpdate();
                } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
                }
            }
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(preparedStatement1);
            closeQuietly(statement1);
            closeQuietly(statement2);
            closeQuietly(statement3);
        }

        return result;
    }

    private PART_OF getPartOfIfAlreadyStored(Organisation org, Organisation mother_org) {
        PART_OF part_of = null;
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement(SQL_SELECT_PARTOF_BY_orgID_motherorgID);
            statement.setLong(1, org.getOrganisationId());
            statement.setLong(2, mother_org.getOrganisationId());
            ResultSet rs = statement.executeQuery();
            if (rs.first()) {
                try {
                    part_of = new PART_OF(mother_org,
                            Utilities.parseStringDate(rs.getString("from_date")),
                            Utilities.parseStringDate(rs.getString("until_date"))
                    );
                } catch (ParseException ex) {
                    LOGGER.error("Error: ", ex);
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(statement);
        }
        return part_of;
    }

    @Override
    public Organisation find(Long id) {
        Organisation organisation = new Organisation();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;

        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_ORG_BY_ID);
            preparedStatement1 = this.connect.prepareStatement(SQL_SELECT_ORGNAMES_BY_ID);
            preparedStatement2 = this.connect.prepareStatement(SQL_SELECT_ORGIDENTIFIERS_BY_ID);

            preparedStatement.setLong(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            try {
                if (rs.first()) {

                    organisation = new Organisation(
                            id,
                            rs.getString("org.type"),
                            rs.getString("org.status"),
                            rs.getString("org.url"),
                            (new ArrayList<Organisation_Name>()),
                            null,
                            (new ArrayList<Organisation_Identifier>()), null
                    );

                    preparedStatement1.setLong(1, id);
                    ResultSet rs1 = preparedStatement1.executeQuery();

                    while (rs1.next()) {
                        if (rs1.getString("orgname.name") != null) {
                            organisation.getNames().add(new Organisation_Name(rs1.getLong("orgname.organisation_nameID"), rs1.getString("orgname.name"), Utilities.parseStringDate(rs1.getString("orgname.lastupdate_date"))));
                        }
                    }

                    preparedStatement2.setLong(1, id);
                    ResultSet rs2 = preparedStatement2.executeQuery();
                    while (rs2.next()) {
                        organisation.getOrganisation_identifiers().add(new Organisation_Identifier(rs2.getString("org_id.ID"), rs2.getString("org_id.Type")));
                    }
                    organisation = setOrganisationParents(organisation);
                }
            } catch (ParseException ex) {
                LOGGER.error("Error: ", ex);
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(preparedStatement1);
            closeQuietly(preparedStatement2);
            closeQuietly(preparedStatement);
        }
        return organisation;
    }

    private Organisation setOrganisationParents(Organisation org) {

        PreparedStatement statement = null;

        try {
            statement = this.connect.prepareStatement(SQL_SELECT_ORGPARENTS_BY_ID);
            statement.setLong(1, org.getOrganisationId());
            // process the results
            ResultSet rs = statement.executeQuery();
            Organisation parentOrg = null;
            while (rs.next()) {
                Long motherID = rs.getLong("organisation_motherID");

                if (!org.getOrganisationId().equals(motherID)) {
                    PART_OF part_of = new PART_OF();
                    parentOrg = find(motherID);

                    part_of.setOrganisation_mother(parentOrg);
//                part_of.setBeginDate(beginDate);
//                part_of.setBeginDate(beginDate);
                    org.addRel(part_of);
                }
            }

        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(statement);
        }
        return org;
    }

    public List<PART_OF> findMothers(Long id) {
        List<PART_OF> rels = new ArrayList<PART_OF>();
        Organisation org = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;

        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_ORGPARENTS_BY_ID);
            preparedStatement1 = this.connect.prepareStatement(SQL_SELECT_ORG_BY_ID);
            preparedStatement2 = this.connect.prepareStatement(SQL_SELECT_ORGNAMES_BY_ID);
            preparedStatement3 = this.connect.prepareStatement(SQL_SELECT_ORGIDENTIFIERS_BY_ID);
            preparedStatement.setLong(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            try {
                while (rs.next()) {
                    preparedStatement1.setLong(1, rs.getLong("organisation_motherID"));
                    ResultSet rs1 = preparedStatement1.executeQuery();
                    if (rs1.first()) {
                        org = new Organisation(
                                rs.getLong("organisation_motherID"),
                                rs1.getString("org.type"),
                                rs1.getString("org.status"),
                                rs1.getString("org.url"),
                                (new ArrayList<Organisation_Name>()),
                                new ArrayList<PART_OF>(),
                                (new ArrayList<Organisation_Identifier>()), null
                        );
                        preparedStatement2.setLong(1, id);

                        ResultSet rs2 = preparedStatement2.executeQuery();
                        while (rs2.next()) {
                            if (rs2.getString("orgname.name") != null) {
                                org.getNames().add(new Organisation_Name(rs2.getLong("orgname.organisation_nameID"), rs2.getString("orgname.name"), Utilities.parseStringDate(rs2.getString("orgname.lastupdate_date"))));
                            }
                        }
                        preparedStatement3.setLong(1, id);
                        ResultSet rs3 = preparedStatement3.executeQuery();
                        while (rs3.next()) {
                            org.getOrganisation_identifiers().add(new Organisation_Identifier(rs3.getString("org_id.ID"), rs3.getString("org_id.Type")));
                        }
                    }
                    rels.add(new PART_OF(org, Utilities.parseStringDate(rs.getString("from_date")), Utilities.parseStringDate(rs.getString("until_date"))));
                }
            } catch (ParseException ex) {
                LOGGER.error("Error: ", ex);
            }

        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(preparedStatement);
            closeQuietly(preparedStatement1);
            closeQuietly(preparedStatement2);
            closeQuietly(preparedStatement3);
        }
        return rels;
    }

    public List<Organisation_Name> getOrganisationNames(Long id) {
        List<Organisation_Name> names = new ArrayList<Organisation_Name>();
        Organisation org = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_ORGNAME_BY_ORGID);
            preparedStatement.setLong(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            try {
                while (rs.next()) {
                    names.add(new Organisation_Name(rs.getLong("organisation_nameID"), rs.getString("name"), Utilities.parseStringDate(rs.getString("lastupdate_date"))));
                }
            } catch (ParseException ex) {
                LOGGER.error("Error: ", ex);
            }

        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(preparedStatement);
        }
        return names;
    }

    public List<Organisation> findAllOrganisations() {
        List<Organisation> organisations = new ArrayList<Organisation>();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECTALL);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                organisations.add(find(rs.getLong("org.organisationID")));
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(preparedStatement);
        }
        return organisations;
    }

    public List<Organisation> getOrganisationsByDocId(Long docId) {
        List<Organisation> orgs = new ArrayList<Organisation>();

        PreparedStatement ps = null;
        PreparedStatement ps1 = null;

        Organisation organisation = null;
        try {
            ps = this.connect.prepareStatement(SQL_SELECT_AUTHORSID_BY_DOCID);
            ps.setLong(1, docId);
            ps1 = this.connect.prepareStatement(SQL_SELECT_ORGID_BY_PERSONID);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ps1.setLong(1, rs.getLong("personID"));
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    organisation = find(rs1.getLong("organisationID"));
                    orgs.add(organisation);
                }

            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(ps);
            closeQuietly(ps1);
        }
        return orgs;
    }

    protected Long getOrgEntityIfAlreadyStored(Organisation obj) {
        Long orgId = null;
        PreparedStatement ps = null;

        try {
            ps = connect.prepareStatement(READ_QUERY_ORG_BY_IDENTIFIER);
            for (int i = 0; i < obj.getOrganisation_identifiers().size(); i++) {
                ps.setString(1, obj.getOrganisation_identifiers().get(i).getId());
                ps.setString(2, obj.getOrganisation_identifiers().get(i).getType());
                ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    orgId = rs.getLong("org_id.organisationID");
                    return orgId;
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(ps);
        }
        return orgId;
    }

    public List<Affiliation> getAffiliationByPersonID(Person person) {
        List<Affiliation> affiliations = new ArrayList<Affiliation>();

        Affiliation affiliation = null;
        PreparedStatement ps = null;
        try {
            ps = this.connect.prepareStatement(SQL_SELECT_AFFILIATION_BY_PERSONID);
            ps.setLong(1, person.getPersonId());
            // process the results
            ResultSet rs = ps.executeQuery();
            Organisation org = null;
            while (rs.next()) {
                try {
                    affiliation = new Affiliation(
                            rs.getLong("affiliationID"),
                            new ArrayList<Organisation>(),
                            person,
                            Utilities.parseStringDate(rs.getString("from_date")),
                            Utilities.parseStringDate(rs.getString("until_date"))
                    );
                    org = find(rs.getLong("organisationID"));
                    affiliation.addOrganisation(org);
                } catch (ParseException ex) {
                    LOGGER.error("Error: ", ex);
                }
                affiliations.add(affiliation);
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(ps);
        }
        return affiliations;
    }

    public Document_Organisation getOrganisationByDocumentID(String docID) {
        Document_Organisation dorg = new Document_Organisation();
        dorg.setDoc(new Document(docID, null, null));
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_ORGANISATIONS_BY_DOCUMENT);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setString(1, docID);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                dorg.addOrg(find(result.getLong("organisationID")));
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(preparedStatement);
        }

        return dorg;
    }

}
