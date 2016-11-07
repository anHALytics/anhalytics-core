package fr.inria.anhalytics.commons.dao;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.commons.utilities.Utilities;
import fr.inria.anhalytics.commons.entities.Author;
import fr.inria.anhalytics.commons.entities.Editor;
import fr.inria.anhalytics.commons.entities.Person;
import fr.inria.anhalytics.commons.entities.Person_Identifier;
import fr.inria.anhalytics.commons.entities.Person_Name;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author azhar
 */
public class PersonDAO extends DAO<Person, Long> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PersonDAO.class);
    private static final String SQL_INSERT_PERSON
            = "INSERT INTO PERSON (title, photo, url, email, phone) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_PERSON_NAME
            = "INSERT INTO PERSON_NAME (personID, fullname, forename ,middlename , surname, title, publication_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_PERSON_IDENTIFIER
            = "INSERT INTO PERSON_IDENTIFIER (personID, ID, type) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_AUTHOR
            = "INSERT INTO AUTHORSHIP (docID, personID, rank, corresp) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_EDITOR
            = "INSERT INTO EDITORSHIP (rank, personID, publicationID) VALUES (?, ?, ?)";

    private static final String READ_QUERY_PERSON_IDENTIFIER_BY_ID = "SELECT * FROM PERSON_IDENTIFIER pi WHERE pi.personID = ?";

    private static final String READ_QUERY_AUTHORS_BY_DOCID = "SELECT personID FROM AUTHORSHIP WHERE docID = ?";

    private static final String READ_QUERY_EDITORS_BY_PUBID = "SELECT personID FROM EDITORSHIP WHERE publicationID = ?";

    private static final String SQL_SELECT_PERSON_BY_ORGID = "SELECT DISTINCT personID FROM AFFILIATION WHERE organisationID = ?";

    private static final String READ_QUERY_PERSON_BY_ID
            = "SELECT * FROM PERSON p WHERE p.personID = ?";

    private static final String READ_QUERY_PERSON_NAME_BY_ID
            = "SELECT * FROM PERSON_NAME pn WHERE pn.personID = ? GROUP BY pn.publication_date ORDER BY pn.publication_date DESC";
    private static final String READ_QUERY_AUTHORS
            = "SELECT DISTINCT personID FROM AUTHORSHIP";

    private static final String READ_QUERY_PERSON_BY_IDENTIFIER = "SELECT pi.personID FROM PERSON_IDENTIFIER pi WHERE pi.ID = ?";

    private static final String READ_QUERY_PERSON_IDENTIFIER = "SELECT * FROM PERSON_IDENTIFIER pi WHERE pi.ID = ? AND pi.type= ? AND pi.personID=?";

    private static final String UPDATE_PERSON = "UPDATE PERSON SET title = ? ,photo = ? ,url = ?, email = ?, phone = ? WHERE personID = ?";

    private static final String DELETE_PERSON = "DELETE PERSON, PERSON_NAME, PERSON_IDENTIFIER WHERE personID = ?";

    public PersonDAO(Connection conn) {
        super(conn);
    }

    private Long getEntityIdIfAlreadyStored(Person toBeStored) throws SQLException {
        Long personId = null;
        PreparedStatement statement = connect.prepareStatement(READ_QUERY_PERSON_BY_IDENTIFIER);
        for (Person_Identifier id : toBeStored.getPerson_identifiers()) {
            statement.setString(1, id.getId());
            ResultSet rs = statement.executeQuery();
            if (rs.first()) {
                personId = rs.getLong("pi.personID");
                return personId;
            }
        }
        statement.close();
        return personId;
    }

    public boolean createAuthor(Author author) throws SQLException {
        boolean result = false;
        create(author.getPerson());
        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT_AUTHOR);
        statement.setString(1, author.getDocument().getDocID());
        statement.setLong(2, author.getPerson().getPersonId());
        statement.setInt(3, author.getRank());
        statement.setInt(4, author.getCorrep());
        int code = statement.executeUpdate();
        statement.close();
        result = true;
        return result;
    }

    public boolean createEditor(Editor editor) throws SQLException {
        boolean result = false;
        create(editor.getPerson());
        PreparedStatement statement;
        statement = connect.prepareStatement(SQL_INSERT_EDITOR);
        statement.setInt(1, editor.getRank());
        statement.setLong(2, editor.getPerson().getPersonId());
        statement.setLong(3, editor.getPublication().getPublicationID());
        int code = statement.executeUpdate();
        statement.close();
        result = true;
        return result;
    }

    @Override
    public boolean create(Person obj) throws SQLException {
        boolean result = false;
        if (obj.getPersonId() != null) {
            throw new IllegalArgumentException("Person is already created, the Person ID is not null.");
        }

        Long persId = getEntityIdIfAlreadyStored(obj);
        if (persId != null) {
            obj.setPersonId(persId);
            update(obj);
        } else {
            PreparedStatement statement;
            PreparedStatement statement1;
            PreparedStatement statement2;
            statement = connect.prepareStatement(SQL_INSERT_PERSON, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getTitle());
            statement.setString(2, obj.getPhoto());
            statement.setString(3, obj.getUrl());
            statement.setString(4, obj.getEmail());
            statement.setString(5, obj.getPhone());
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setPersonId(rs.getLong(1));
            }
            statement.close();

            statement1 = connect.prepareStatement(SQL_INSERT_PERSON_NAME);
            for (Person_Name pn : obj.getPerson_names()) {
                statement1.setLong(1, obj.getPersonId());
                statement1.setString(2, pn.getFullname());
                statement1.setString(3, pn.getForename());
                statement1.setString(4, pn.getMiddlename());
                statement1.setString(5, pn.getSurname());
                statement1.setString(6, pn.getTitle());
                if (pn.getPublication_date() == null) {
                    statement1.setDate(7, new java.sql.Date(00000000L));
                } else {
                    statement1.setDate(7, new java.sql.Date(pn.getPublication_date().getTime()));
                }

                int code1 = statement1.executeUpdate();
            }

            statement2 = connect.prepareStatement(SQL_INSERT_PERSON_IDENTIFIER);
            if (obj.getPerson_identifiers() != null) {
                for (Person_Identifier pi : obj.getPerson_identifiers()) {
                    if (!isIdentifierIfAlreadyExisting(pi, obj.getPersonId())) {
                        statement2.setLong(1, obj.getPersonId());
                        statement2.setString(2, pi.getId());
                        statement2.setString(3, pi.getType());

                        int code2 = statement2.executeUpdate();
                    }
                }
            }
            statement2.close();
            result = true;
        }
        return result;
    }

    @Override
    public boolean delete(Person obj) throws SQLException {
        boolean result = false;
        PreparedStatement preparedStatement = this.connect.prepareStatement(DELETE_PERSON);
        preparedStatement.setLong(1, obj.getPersonId());
        preparedStatement.executeUpdate();
        preparedStatement.close();
        result = true;
        return result;
    }

    @Override
    public boolean update(Person obj) throws SQLException {
        boolean result = false;
        PreparedStatement preparedStatement = null, preparedStatement2 = null, preparedStatement3 = null;
        preparedStatement = this.connect.prepareStatement(UPDATE_PERSON);
        preparedStatement.setString(1, obj.getTitle());
        preparedStatement.setString(2, obj.getPhoto());
        preparedStatement.setString(3, obj.getUrl());
        preparedStatement.setString(4, obj.getEmail());
        preparedStatement.setString(5, obj.getPhone());
        preparedStatement.setLong(6, obj.getPersonId());
        int code1 = preparedStatement.executeUpdate();

        preparedStatement2 = this.connect.prepareStatement(SQL_INSERT_PERSON_NAME);
       
            for (Person_Name pn : obj.getPerson_names()) {
                 try {
                preparedStatement2.setLong(1, obj.getPersonId());
                preparedStatement2.setString(2, pn.getFullname());
                preparedStatement2.setString(3, pn.getForename());
                preparedStatement2.setString(4, pn.getMiddlename());
                preparedStatement2.setString(5, pn.getSurname());
                preparedStatement2.setString(6, pn.getTitle());
                if (pn.getPublication_date() == null) {
                    preparedStatement2.setDate(7, new java.sql.Date(00000000L));
                } else {
                    preparedStatement2.setDate(7, new java.sql.Date(pn.getPublication_date().getTime()));
                }
                int code2 = preparedStatement2.executeUpdate();
                } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
            //e.printStackTrace();
        } 
            }
            preparedStatement2.close();
        
        
        preparedStatement3 = connect.prepareStatement(SQL_INSERT_PERSON_IDENTIFIER);

        if (obj.getPerson_identifiers() != null) {
            for (Person_Identifier pi : obj.getPerson_identifiers()) {
                try {
                    preparedStatement3.setLong(1, obj.getPersonId());
                    preparedStatement3.setString(2, pi.getId());
                    preparedStatement3.setString(3, pi.getType());

                    int code3 = preparedStatement3.executeUpdate();
                } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e) {
                } 
            }
                    preparedStatement3.close();
                
        }

        result = true;

        preparedStatement.close();
        return result;
    }

    /**
     * find person with all names that have.
     */
    @Override
    public Person find(Long id) throws SQLException {
        Person person = null;
        List<Person_Name> person_names = new ArrayList<Person_Name>();
        List<Person_Identifier> person_identifiers = new ArrayList<Person_Identifier>();
        PreparedStatement preparedStatement = this.connect.prepareStatement(READ_QUERY_PERSON_BY_ID),
                preparedStatement1 = this.connect.prepareStatement(READ_QUERY_PERSON_NAME_BY_ID),
                preparedStatement2 = this.connect.prepareStatement(READ_QUERY_PERSON_IDENTIFIER_BY_ID);
        try {
            
            preparedStatement.setLong(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                person = new Person(
                        id,
                        rs.getString("p.title"),
                        rs.getString("p.photo"),
                        rs.getString("p.url"),
                        rs.getString("p.email"),
                        rs.getString("p.phone"),
                        new ArrayList<Person_Identifier>(),
                        new ArrayList<Person_Name>()
                );
                
                preparedStatement1.setLong(1, id);
                ResultSet rs1 = preparedStatement1.executeQuery();
                Person_Name pn = null;
                while (rs1.next()) {
                    try {
                        pn = new Person_Name(rs1.getLong("pn.person_nameID"), id, rs1.getString("pn.fullname"), rs1.getString("pn.forename"), rs1.getString("pn.middlename"), rs1.getString("pn.surname"), rs1.getString("pn.title"), Utilities.parseStringDate(rs1.getString("pn.publication_date")));
                        if(!person_names.contains(pn))
                        person_names.add(pn);
                    } catch (ParseException ex) {
                        Logger.getLogger(PersonDAO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                person.setPerson_names(person_names);
                preparedStatement2.setLong(1, id);
                ResultSet rs2 = preparedStatement2.executeQuery();
                while (rs2.next()) {
                    person_identifiers.add(new Person_Identifier(rs2.getString("pi.ID"), rs2.getString("pi.Type")));

                }
                person.setPerson_identifiers(person_identifiers);
            }

        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
            preparedStatement1.close();
            preparedStatement2.close();
        }
        return person;
    }

    public Map<Long, Person> findAllAuthors() throws SQLException {
        HashMap<Long, Person> persons = new HashMap<Long, Person>();
        PreparedStatement 
            preparedStatement = this.connect.prepareStatement(READ_QUERY_AUTHORS);
        try {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                persons.put(rs.getLong("personID"), find(rs.getLong("personID")));
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            preparedStatement.close();
        }
        return persons;
    }

    public Map<Long, Person> getAuthorsByDocId(String docId) throws SQLException {
        HashMap<Long, Person> persons = new HashMap<Long, Person>();
        PreparedStatement ps = this.connect.prepareStatement(READ_QUERY_AUTHORS_BY_DOCID);
        try {
            Person person = null;

            ps.setString(1, docId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                person = find(rs.getLong("personID"));
                persons.put(rs.getLong("personID"), person);
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            ps.close();
        }
        return persons;
    }

    public Map<Long, Person> getEditorsByPubId(Long pubId) throws SQLException {
        HashMap<Long, Person> persons = new HashMap<Long, Person>();
        PreparedStatement ps = this.connect.prepareStatement(READ_QUERY_EDITORS_BY_PUBID);
        try {
            Person person = null;
            ps.setLong(1, pubId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                person = find(rs.getLong("personID"));
                if (person != null) {
                    persons.put(rs.getLong("personID"), person);
                }
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            ps.close();
        }
        return persons;

    }

    private boolean isIdentifierIfAlreadyExisting(Person_Identifier id, Long personID) throws SQLException {
        PreparedStatement statement;
        statement = connect.prepareStatement(READ_QUERY_PERSON_IDENTIFIER);
        statement.setString(1, id.getId());
        statement.setString(2, id.getType());
        statement.setLong(3, personID);
        ResultSet rs = statement.executeQuery();
        if (rs.first()) {
            return true;
        }
        statement.close();
        return false;
    }

    public Map<Long, Person> getPersonsByOrgID(Long orgID) throws SQLException {
        HashMap<Long, Person> persons = new HashMap<Long, Person>();
        PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_PERSON_BY_ORGID);;
        try {
            Person person = null;
            ps.setLong(1, orgID);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                person = find(rs.getLong("personID"));
                persons.put(rs.getLong("personID"), person);
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            ps.close();
        }
        return persons;
    }
}
