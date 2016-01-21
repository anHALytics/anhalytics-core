package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.entities.Address;
import fr.inria.anhalytics.entities.Conference;
import fr.inria.anhalytics.entities.Conference_Event;
import fr.inria.anhalytics.entities.Monograph;
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
public class Conference_EventDAO extends DAO<Conference_Event> {

    private static final String SQL_INSERT
            = "INSERT INTO CONFERENCE_EVENT (conferenceID, addressID, start_date, end_date, monographID) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_CONFERENCE
            = "INSERT INTO CONFERENCE (title) VALUES (?)";

    public Conference_EventDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Conference_Event obj) {
        boolean result = false;
        if (obj.getConf_eventID() != null) {
            throw new IllegalArgumentException("Conference_Event is already created, the Conference_Event ID is not null.");
        }
        PreparedStatement statement;
        PreparedStatement statement1;
        try {
            statement1 = connect.prepareStatement(SQL_INSERT_CONFERENCE, Statement.RETURN_GENERATED_KEYS);
            if (obj.getConference().getTitle() == null) {
                statement1.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement1.setString(1, obj.getConference().getTitle());
            }
            int code1 = statement1.executeUpdate();
            ResultSet rs1 = statement1.getGeneratedKeys();

            if (rs1.next()) {
                obj.getConference().setConfID(rs1.getLong(1));
            }

            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

            statement.setLong(1, obj.getConference().getConfID());

            if (obj.getConference() == null) {
                statement.setLong(2, java.sql.Types.INTEGER);
            } else {
                statement.setLong(2, obj.getAddress().getAddressId());
            }
            statement.setString(3, obj.getStart_date());
            statement.setString(4, obj.getEnd_date());
            statement.setLong(5, obj.getMonograph().getMonographID());
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setConf_eventID(rs.getLong(1));
            }

            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(Conference_EventDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public boolean delete(Conference_Event obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Conference_Event obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Conference_Event find(Long id) {
        Conference_Event conference_event = new Conference_Event();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM monograph WHERE monographID = " + id);
            if (result.first()) {
                conference_event = new Conference_Event(
                        id,
                        result.getString("start_date"),
                        result.getString("end_date"),
                        new Monograph(),
                        new Conference(),
                        new Address()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conference_event;
    }

}
