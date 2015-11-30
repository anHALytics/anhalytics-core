package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.entities.Collection;
import fr.inria.anhalytics.entities.In_Serial;
import fr.inria.anhalytics.entities.Journal;
import fr.inria.anhalytics.entities.Monograph;
import fr.inria.anhalytics.entities.Serial_Identifier;
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
public class In_SerialDAO extends DAO<In_Serial> {
    
    private static final String SQL_INSERT
            = "INSERT INTO IN_SERIAL (monographID, collectionID, journalID, volume, number) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_COLLECTION
            = "INSERT INTO COLLECTION (title) VALUES (?)";

    private static final String SQL_INSERT_JOURNAL
            = "INSERT INTO JOURNAL (title) VALUES (?)";
    
    private static final String SQL_INSERT_SERIAL_IDENTIFIER
            = "INSERT INTO SERIAL_IDENTIFIER (id, type, journalID, collectionID) VALUES (?, ?, ?, ?)";

    
    public In_SerialDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(In_Serial obj) {
        boolean result = false;
        /*if (obj.getMg()!= null) {
            throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
        }*/

        PreparedStatement statement;
        PreparedStatement statement1;
        PreparedStatement statement2;
        try {
            statement = connect.prepareStatement(SQL_INSERT_JOURNAL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getJ().getTitle());
            
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.getJ().setJournalID(rs.getLong(1));
            }

            result = true;
            
            
            statement1 = connect.prepareStatement(SQL_INSERT_COLLECTION, Statement.RETURN_GENERATED_KEYS);
            statement1.setString(1, obj.getC().getTitle());
            
            int code1 = statement1.executeUpdate();
            ResultSet rs1 = statement1.getGeneratedKeys();

            if (rs1.next()) {
                obj.getC().setCollectionID(rs1.getLong(1));
            }
            
            statement2 = connect.prepareStatement(SQL_INSERT);
            
            statement2.setLong(1, obj.getMg().getMonographID());
            statement2.setLong(2, obj.getC().getCollectionID());
            statement2.setLong(3, obj.getJ().getJournalID());
            statement2.setString(4, obj.getVolume());
            statement2.setString(5, obj.getNumber());
            
            int code2 = statement2.executeUpdate();

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;    
    }
    
    public boolean createSerial(In_Serial obj, Serial_Identifier obj1) {
        create(obj);
        boolean result = false;
        /*if (obj.getMg()!= null) {
            throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
        }*/

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT_SERIAL_IDENTIFIER, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj1.getId());
            statement.setString(2, obj1.getType());
            statement.setLong(3, obj.getJ().getJournalID());
            statement.setLong(4, obj.getC().getCollectionID());
            
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj1.setSerial_IdentifierID(rs.getLong(1));
            }

            result = true;
            
            
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
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

    @Override
    public In_Serial find(Long id) {
        In_Serial in_serial = new In_Serial();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM IN_SERIAL WHERE monographID = " + id);
            if (result.first()) {
                in_serial = new In_Serial(
                        new Monograph(),
                        new Journal(),
                        new Collection(),
                        result.getString("volume"),
                        result.getString("number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return in_serial;
    }
    
}
