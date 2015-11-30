package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.entities.Author;
import fr.inria.anhalytics.entities.Editor;
import fr.inria.anhalytics.entities.Monograph;
import fr.inria.anhalytics.entities.Organisation;
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

    private static final String SQL_INSERT
            = "INSERT INTO PERSON (title, photo, url) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_PERSON_NAME
            = "INSERT INTO PERSON_NAME (personID, fullname, forename ,middlename , surname, title) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_PERSON_IDENTIFIER
            = "INSERT INTO PERSON_IDENTIFIER (personID, ID, type) VALUES (?, ?, ?)";

    private static final String SQL_INSERT_AUTHOR
            = "INSERT INTO AUTHOR (docID, personID, rank, corresp) VALUES (?, ?, ?, ?)";

    private static final String SQL_INSERT_EDITOR
            = "INSERT INTO EDITOR (rank, personID, publicationID) VALUES (?, ?, ?)";

    private static final String READ_QUERY_AUTHORS_BY_DOCID = "SELECT personID FROM AUTHOR WHERE docID = ?";

    public PersonDAO(Connection conn) {
        super(conn);
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

        PreparedStatement statement;
        PreparedStatement statement1;
        PreparedStatement statement2;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getTitle());
            statement.setString(2, obj.getPhoto());
            statement.setString(3, obj.getUrl());
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

            for (Person_Identifier pi : obj.getPerson_identifiers()) {
                statement2 = connect.prepareStatement(SQL_INSERT_PERSON_IDENTIFIER);
                statement2.setLong(1, obj.getPersonId());
                statement2.setString(2, pi.getId());
                statement2.setString(3, pi.getType());

                int code2 = statement2.executeUpdate();
            }
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public boolean delete(Person obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Person obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Person find(Long id) {
        Person person = new Person();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT p.title, p.photo, p.url, pn.fullname, pn.forename, pn.middlename, pn.surname FROM PERSON p, PERSON_NAME pn WHERE p.personID = " + id + " AND pn.personID = " + id);
            if (result.first()) {
                person = new Person(
                        id,
                        result.getString("p.title"),
                        result.getString("p.photo"),
                        result.getString("pn.fullname"),
                        result.getString("pn.forename"),
                        result.getString("pn.middlename"),
                        result.getString("pn.surname"),
                        result.getString("p.url"),
                        new ArrayList<Person_Identifier>());
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
