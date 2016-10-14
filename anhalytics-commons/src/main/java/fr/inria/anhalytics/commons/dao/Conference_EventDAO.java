package fr.inria.anhalytics.commons.dao;

import fr.inria.anhalytics.commons.entities.Address;
import fr.inria.anhalytics.commons.entities.Conference;
import fr.inria.anhalytics.commons.entities.Conference_Event;
import fr.inria.anhalytics.commons.entities.Country;
import fr.inria.anhalytics.commons.entities.Monograph;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author azhar
 */
public class Conference_EventDAO extends DAO<Conference_Event, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO CONFERENCE_EVENT (conferenceID, addressID, start_date, end_date, monographID) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_CONFERENCE
            = "INSERT INTO CONFERENCE (title) VALUES (?)";

    private static final String SQL_SELECT_CONFERENCE
            = "SELECT * FROM CONFERENCE WHERE title = ?";

    //private static final String SQL_SELECT_CONFERENCE
    //       = "SELECT * FROM CONFERENCE_EVENT, CONFERENCE, ADDRESS WHERE conference_eventID = ? AND CONFERENCE_EVENT.conferenceID = CONFERENCE.conferenceID AND ADDRESS.addressID = CONFERENCE_EVENT.addressID";
    private static final String SQL_SELECT_CONFERENCE_BY_MONOGR
            = "SELECT * FROM CONFERENCE_EVENT, CONFERENCE, ADDRESS  WHERE monographID = ? AND CONFERENCE_EVENT.conferenceID = CONFERENCE.conferenceID AND ADDRESS.addressID = CONFERENCE_EVENT.addressID";

    private static final String SQL_SELECT_MONOGR_BY_ID
            = "SELECT * FROM MONOGRAPH WHERE monographID = ?";

    public Conference_EventDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Conference_Event obj) throws SQLException {
        boolean result = false;
        if (obj.getConf_eventID() != null) {
            throw new IllegalArgumentException("Conference_Event is already created, the Conference_Event ID is not null.");
        }
        PreparedStatement statement;
        PreparedStatement statement1;
        Conference conference = findConferenceByTitle(obj.getConference().getTitle());
        if (conference != null) {
            obj.getConference().setConfID(conference.getConfID());
        } else {
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
            statement1.close();
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
        statement.close();
        result = true;
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
    public Conference_Event find(Long id) throws SQLException {
        Conference_Event conference_event = null;
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_MONOGR_BY_ID);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                conference_event = new Conference_Event(
                        id,
                        result.getString("start_date"),
                        result.getString("end_date"),
                        new Monograph(),
                        new Conference(result.getLong("conferenceID"), result.getString("title")),
                        new Address()
                );
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            preparedStatement.close();
        }
        return conference_event;
    }

    public Conference findConferenceByTitle(String title) throws SQLException {
        Conference conference = null;
        PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_CONFERENCE);
        try {
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setString(1, title);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                conference = new Conference(result.getLong("conferenceID"), result.getString("title"));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            preparedStatement.close();
        }
        return conference;
    }

    public Conference_Event findByMonograph(Long id) throws SQLException {
        Conference_Event conference_event = null;
        PreparedStatement ps = this.connect.prepareStatement(SQL_SELECT_CONFERENCE_BY_MONOGR);
        try {
            ps.setLong(1, id);
            // process the results
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                conference_event = new Conference_Event(
                        rs.getLong("conference_eventID"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        new Monograph(),
                        new Conference(rs.getLong("conferenceID"), rs.getString("title")),
                        new Address(rs.getLong("addressID"), rs.getString("addrLine"), rs.getString("postBox"), rs.getString("postCode"), rs.getString("settlement"), rs.getString("region"), rs.getString("country"), new Country())
                );
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            ps.close();
        }
        return conference_event;
    }

}
