package fr.inria.anhalytics.commons.dao;

import fr.inria.anhalytics.commons.entities.Collection;
import fr.inria.anhalytics.commons.entities.In_Serial;
import fr.inria.anhalytics.commons.entities.Journal;
import fr.inria.anhalytics.commons.entities.Monograph;
import fr.inria.anhalytics.commons.entities.Serial_Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author azhar
 */
public class In_SerialDAO extends DAO<In_Serial, Long> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(In_SerialDAO.class);
    
    private static final String SQL_INSERT
            = "INSERT INTO IN_SERIAL (monographID, collectionID, journalID, volume, number) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SQL_INSERT_COLLECTION
            = "INSERT INTO COLLECTION (title) VALUES (?)";
    
    private static final String SQL_INSERT_JOURNAL
            = "INSERT INTO JOURNAL (title) VALUES (?)";
    
    private static final String SQL_INSERT_SERIAL_IDENTIFIER
            = "INSERT INTO SERIAL_IDENTIFIER (id, type, journalID, collectionID) VALUES (?, ?, ?, ?)";
    
    private static final String SQL_INSERT_SERIAL_BY_MONOGRID
            = "SELECT * FROM MONOGRAPH, IN_SERIAL LEFT JOIN COLLECTION ON COLLECTION.collectionID = IN_SERIAL.collectionID LEFT JOIN JOURNAL ON JOURNAL.journalID = IN_SERIAL.journalID WHERE IN_SERIAL.monographID = ? AND MONOGRAPH.monographID = ?";
    
    private static final String SQL_SELECT_COLLECTION
            = "SELECT * FROM COLLECTION WHERE title = ?";
    private static final String SQL_SELECT_JOURNAL
            = "SELECT * FROM JOURNAL WHERE title = ?";
    
    public In_SerialDAO(Connection conn) {
        super(conn);
    }
    
    @Override
    public boolean create(In_Serial obj) throws SQLException {
        boolean result = false;
        /*if (obj.getMg()!= null) {
         throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
         }*/
        
        PreparedStatement statement = null;
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        if (obj.getJ() != null) {
            Journal journal = findJournalByTitle(obj.getJ().getTitle());
            if (journal != null) {
                obj.getJ().setJournalID(journal.getJournalID());
            } else {
                
                try {
                    statement = connect.prepareStatement(SQL_INSERT_JOURNAL, Statement.RETURN_GENERATED_KEYS);
                    statement.setString(1, obj.getJ().getTitle());
                    
                    int code = statement.executeUpdate();
                    ResultSet rs = statement.getGeneratedKeys();
                    
                    if (rs.next()) {
                        obj.getJ().setJournalID(rs.getLong(1));
                    }
                } finally {
                    closeQuietly(statement);
                }
            }
            
        }
        if (obj.getC() != null) {
            Collection collection = findCollectionByTitle(obj.getC().getTitle());
            
            if (collection != null) {
                obj.getC().setCollectionID(collection.getCollectionID());
            } else {
                try {
                    statement1 = connect.prepareStatement(SQL_INSERT_COLLECTION, Statement.RETURN_GENERATED_KEYS);
                    statement1.setString(1, obj.getC().getTitle());
                    
                    int code1 = statement1.executeUpdate();
                    ResultSet rs1 = statement1.getGeneratedKeys();
                    
                    if (rs1.next()) {
                        obj.getC().setCollectionID(rs1.getLong(1));
                    }
                } finally {
                    closeQuietly(statement1);
                }
            }
        }
        
        try {
            statement2 = connect.prepareStatement(SQL_INSERT);
            statement2.setLong(1, obj.getMg().getMonographID());
            
            if (obj.getC() == null) {
                statement2.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement2.setLong(2, obj.getC().getCollectionID());
            }
            
            if (obj.getJ() == null) {
                statement2.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement2.setLong(3, obj.getJ().getJournalID());
            }
            statement2.setString(4, obj.getVolume());
            statement2.setString(5, obj.getIssue());
            
            int code2 = statement2.executeUpdate();
            
            result = true;
        } finally {
            closeQuietly(statement2);
        }
        return false;
    }
    
    public boolean createSerialIdentifier(Serial_Identifier obj1) throws SQLException {
        boolean result = false;
        /*if (obj.getMg()!= null) {
         throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
         }*/
        
        PreparedStatement statement = null;
        try {
            statement = connect.prepareStatement(SQL_INSERT_SERIAL_IDENTIFIER, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj1.getId());
            statement.setString(2, obj1.getType());
            statement.setLong(3, obj1.getJournal().getJournalID());
            statement.setLong(4, obj1.getCollection().getCollectionID());
            
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            
            if (rs.next()) {
                obj1.setSerial_IdentifierID(rs.getLong(1));
            }
            
            result = true;
        } finally {
            closeQuietly(statement);
        }
        return false;
    }
    
    @Override
    public boolean delete(In_Serial obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean update(In_Serial obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Collection findCollectionByTitle(String title) throws SQLException {
        Collection collection = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_COLLECTION);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setString(1, title);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                collection = new Collection(result.getLong("collectionID"), result.getString("title"));
            }
        } catch (SQLException sqle) {
            LOGGER.error("Error: ", sqle);
        } finally {
            closeQuietly(preparedStatement);
        }
        return collection;
    }
    
    public Journal findJournalByTitle(String title) throws SQLException {
        Journal journal = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connect.prepareStatement(SQL_SELECT_JOURNAL);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setString(1, title);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                journal = new Journal(result.getLong("journalID"), result.getString("title"));
            }
        } catch (SQLException sqle) {
            LOGGER.error("Error: ", sqle);
        } finally {
            closeQuietly(preparedStatement);
        }
        return journal;
    }
    
    @Override
    public In_Serial find(Long id) throws SQLException {
        In_Serial in_serial = new In_Serial();
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_INSERT_SERIAL_BY_MONOGRID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                in_serial = new In_Serial(
                        new Monograph(result.getLong("MONOGRAPH.monographID"), result.getString("MONOGRAPH.type"), result.getString("MONOGRAPH.title"), result.getString("MONOGRAPH.shortname")),
                        new Journal(result.getLong("JOURNAL.journalID"), result.getString("JOURNAL.title")),
                        new Collection(result.getLong("COLLECTION.collectionID"), result.getString("COLLECTION.title")),
                        result.getString("IN_SERIAL.volume"),
                        result.getString("IN_SERIAL.number"));
            }
        } catch (SQLException ex) {
            LOGGER.error("Error: ", ex);
        } finally {
            closeQuietly(preparedStatement);
        }
        return in_serial;
    }
    
}
