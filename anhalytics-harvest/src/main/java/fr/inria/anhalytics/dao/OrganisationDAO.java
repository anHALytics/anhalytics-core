package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.entities.Organisation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azhar
 */
public class OrganisationDAO extends DAO<Organisation> {
    
    private static final String SQL_INSERT
            = "INSERT INTO ORGANISATION (type, url, STRUTUREcol) VALUES (?, ?, ?)";

    private static final String SQL_INSERT1
            = "INSERT INTO ORGANISATION_NAME (organisationID, name) VALUES (?, ?)";

    public OrganisationDAO(Connection conn) {
        super(conn);
    }

    @Override
    public boolean create(Organisation obj) {
        boolean result = false;
        if (obj.getOrganisationId()!= null) {
            throw new IllegalArgumentException("Organisation is already created, the Organisation ID is not null.");
        }

        PreparedStatement statement;
        PreparedStatement statement1;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getType());
            statement.setString(2, obj.getUrl());
            statement.setString(3, obj.getStructure());
            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setOrganisationId(rs.getLong(1));
            }
            
            statement1 = connect.prepareStatement(SQL_INSERT1);
            statement1.setLong(1, obj.getOrganisationId());
            statement1.setString(2, obj.getName());
            System.out.println(obj.getName());
            int code1 = statement1.executeUpdate();
            System.out.println(obj.getOrganisationId());
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;     }

    @Override
    public boolean delete(Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean update(Organisation obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Organisation find(Long id) {
        Organisation organisation = new Organisation();

        try {
            ResultSet result = this.connect.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM organisation, organisation_name WHERE organisationID = " + id + "AND organisation_name.organisationID = "+id);
            if (result.first()) {
                organisation = new Organisation(
                        id,
                        result.getString("type"),
                        result.getString("url"),
                        result.getString("structure"),
                        result.getString("organisation_name.name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return organisation;
    }
    
}
