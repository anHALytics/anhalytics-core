package fr.inria.anhalytics.dao;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import fr.inria.anhalytics.entities.Author;
import fr.inria.anhalytics.entities.Editor;
import fr.inria.anhalytics.entities.Person;
import fr.inria.anhalytics.entities.Person_Identifier;
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
public class PersonDAO extends DAO<Person> {

    private static final String SQL_INSERT_PERSON
            = "INSERT INTO PERSON (title, photo, url, email) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_PERSON_NAME
            = "INSERT INTO PERSON_NAME (personID, fullname, forename ,middlename , surname, title) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_PERSON_IDENTIFIER
            = "INSERT INTO PERSON_IDENTIFIER (personID, ID, type) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_AUTHOR
            = "INSERT INTO AUTHOR (docID, personID, rank, corresp) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_EDITOR
            = "INSERT INTO EDITOR (rank, personID, publicationID) VALUES (?, ?, ?)";

    private static final String READ_QUERY_AUTHORS_BY_DOCID = "SELECT personID FROM AUTHOR WHERE docID = ?";
    
    private static final String READ_QUERY_PERSON_BY_ID
            = "SELECT p.title, p.photo, p.url, p.email, pn.fullname, pn.forename, pn.middlename, pn.surname, pi.person_identifiersID, pi.ID, pi.Type FROM PERSON p, PERSON_NAME pn, PERSON_IDENTIFIER pi WHERE p.personID = ? AND pn.personID = ? AND pi.personID = ?";

    private static final String READ_QUERY_PERSON_BY_IDENTIFIER = "SELECT pi.personID FROM PERSON_IDENTIFIER pi WHERE pi.ID = ?";

    private static final String UPDATE_PERSON = "UPDATE PERSON SET title = ? ,photo = ? ,url = ? WHERE personID = ?";

    private static final String UPDATE_PERSON_NAME = "UPDATE PERSON_NAME SET fullname = ? ,forename = ? ,middlename = ?, surname = ?, title = ? WHERE personID = ?";

    private static final String DELETE_PERSON = "DELETE PERSON, PERSON_NAME, PERSON_IDENTIFIER WHERE personID = ?";

    
    public PersonDAO(Connection conn) {
        super(conn);
    }

    private Long getEntityIdIfAlreadyStored(Person toBeStored) {
        Long personId = null;
        PreparedStatement statement;
        for (Person_Identifier id : toBeStored.getPerson_identifiers()) {
            try {
                statement = connect.prepareStatement(READ_QUERY_PERSON_BY_IDENTIFIER);
                statement.setString(1, id.getId());
                ResultSet rs = statement.executeQuery();
                if (rs.first()) {
                    personId = rs.getLong("pi.personID");
                    return personId;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return personId;
    }

    public boolean createAuthor(Author author) {
        boolean result = false;
        create(author.getPerson());
        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT_AUTHOR);
            statement.setLong(1, author.getDocument().getDocID());
            statement.setLong(2, author.getPerson().getPersonId());
            statement.setInt(3, author.getRank());
            statement.setInt(4, author.getCorrep());
            int code = statement.executeUpdate();

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public boolean createEditor(Editor editor) {
        boolean result = false;
        create(editor.getPerson());
        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT_EDITOR);
            statement.setInt(1, editor.getRank());
            statement.setLong(2, editor.getPerson().getPersonId());
            statement.setLong(3, editor.getPublication().getPublicationID());
            int code = statement.executeUpdate();

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public boolean create(Person obj) {
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
            try {
                statement = connect.prepareStatement(SQL_INSERT_PERSON, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, obj.getTitle());
                statement.setString(2, obj.getPhoto());
                statement.setString(3, obj.getUrl());
                statement.setString(4, obj.getEmail());
                int code = statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next()) {
                    obj.setPersonId(rs.getLong(1));
                }

                statement1 = connect.prepareStatement(SQL_INSERT_PERSON_NAME);
                statement1.setLong(1, obj.getPersonId());
                statement1.setString(2, obj.getFullname());
                statement1.setString(3, obj.getForename());
                statement1.setString(4, obj.getMiddlename());
                statement1.setString(5, obj.getSurname());
                statement1.setString(6, obj.getTitle());

                int code1 = statement1.executeUpdate();
                if (obj.getPerson_identifiers() != null) {
                    for (Person_Identifier pi : obj.getPerson_identifiers()) {
                        statement2 = connect.prepareStatement(SQL_INSERT_PERSON_IDENTIFIER);
                        statement2.setLong(1, obj.getPersonId());
                        statement2.setString(2, pi.getId());
                        statement2.setString(3, pi.getType());

                        int code2 = statement2.executeUpdate();
                    }
                }
                result = true;

            } catch (SQLException ex) {
                Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    @Override
    public boolean delete(Person obj) {
        boolean result = false;
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(DELETE_PERSON);
            preparedStatement.setLong(1, obj.getPersonId());
            preparedStatement.executeUpdate();
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(PersonDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public boolean update(Person obj) {
        boolean result = false;
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(UPDATE_PERSON);
            preparedStatement.setString(1, obj.getTitle());
            preparedStatement.setString(2, obj.getPhoto());
            preparedStatement.setString(3, obj.getUrl());
            preparedStatement.setLong(4, obj.getPersonId());
            int code1 = preparedStatement.executeUpdate();

            PreparedStatement preparedStatement2 = this.connect.prepareStatement(UPDATE_PERSON_NAME);
            preparedStatement2.setString(1, obj.getFullname());
            preparedStatement2.setString(2, obj.getForename());
            preparedStatement2.setString(3, obj.getMiddlename());
            preparedStatement2.setString(4, obj.getSurname());
            preparedStatement2.setString(5, obj.getTitle());
            preparedStatement2.setLong(6, obj.getPersonId());
            int code2 = preparedStatement2.executeUpdate();

            if (obj.getPerson_identifiers() != null) {
                for (Person_Identifier pi : obj.getPerson_identifiers()) {
                    PreparedStatement preparedStatement3 = connect.prepareStatement(SQL_INSERT_PERSON_IDENTIFIER);
                    preparedStatement3.setLong(1, obj.getPersonId());
                    preparedStatement3.setString(2, pi.getId());
                    preparedStatement3.setString(3, pi.getType());

                    int code3 = preparedStatement3.executeUpdate();
                }
            }
            result = true;
        } catch (MySQLIntegrityConstraintViolationException e) {
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Person find(Long id) {
        Person person = new Person();

        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(READ_QUERY_PERSON_BY_ID);
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                person = new Person(
                        id,
                        rs.getString("p.title"),
                        rs.getString("p.photo"),
                        rs.getString("pn.fullname"),
                        rs.getString("pn.forename"),
                        rs.getString("pn.middlename"),
                        rs.getString("pn.surname"),
                        rs.getString("p.url"),
                        rs.getString("p.email"),
                        new ArrayList<Person_Identifier>());
            }
            while (rs.next()) {
                person.getPerson_identifiers().add(new Person_Identifier(rs.getString("pi.ID"), rs.getString("pi.Type")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    public List<Person> getAuthorsByDocId(Long docId) {
        List<Person> persons = new ArrayList<Person>();

        Person person = null;

        try {
            PreparedStatement ps = this.connect.prepareStatement(READ_QUERY_AUTHORS_BY_DOCID);
            ps.setLong(1, docId);
            // process the results
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                person = find(rs.getLong("personID"));
                persons.add(person);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return persons;
    }

}
