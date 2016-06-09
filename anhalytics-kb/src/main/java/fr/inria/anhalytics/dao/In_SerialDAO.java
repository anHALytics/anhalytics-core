package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.kb.entities.Collection;
import fr.inria.anhalytics.kb.entities.In_Serial;
import fr.inria.anhalytics.kb.entities.Journal;
import fr.inria.anhalytics.kb.entities.Monograph;
import fr.inria.anhalytics.kb.entities.Serial_Identifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class In_SerialDAO extends DAO<In_Serial, Long> {

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
    
    public In_SerialDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(In_Serial obj) throws SQLException {
        boolean result = false;
        /*if (obj.getMg()!= null) {
         throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
         }*/

        PreparedStatement statement;
        PreparedStatement statement1;
        PreparedStatement statement2;
        if (obj.getJ() != null) {
            statement = connect.prepareStatement(SQL_INSERT_JOURNAL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getJ().getTitle());

            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.getJ().setJournalID(rs.getLong(1));
            }
            statement.close();
            result = true;
        }
        if (obj.getC() != null) {
            statement1 = connect.prepareStatement(SQL_INSERT_COLLECTION, Statement.RETURN_GENERATED_KEYS);
            statement1.setString(1, obj.getC().getTitle());

            int code1 = statement1.executeUpdate();
            ResultSet rs1 = statement1.getGeneratedKeys();

            if (rs1.next()) {
                obj.getC().setCollectionID(rs1.getLong(1));
            }
            statement1.close();
        }
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
        statement2.close();
        result = true;
        return false;
    }

    public boolean createSerialIdentifier(Serial_Identifier obj1) throws SQLException {
        boolean result = false;
        /*if (obj.getMg()!= null) {
         throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
         }*/

        PreparedStatement statement;
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
        statement.close();
        result = true;

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

    @Override
    public In_Serial find(Long id) {
        In_Serial in_serial = new In_Serial();
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_INSERT_SERIAL_BY_MONOGRID);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                in_serial = new In_Serial(
                        new Monograph(result.getLong("MONOGRAPH.monographID"), result.getString("MONOGRAPH.type"), result.getString("MONOGRAPH.title"), result.getString("MONOGRAPH.shortname")),
                        new Journal(result.getLong("JOURNAL.journalID"), result.getString("JOURNAL.title")),
                        new Collection(result.getLong("COLLECTION.collectionID"), result.getString("COLLECTION.title")),
                        result.getString("IN_SERIAL.volume"),
                        result.getString("IN_SERIAL.number"));
            }
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return in_serial;
    }

}
