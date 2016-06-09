/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.anhalytics.dao;

import fr.inria.anhalytics.dao.DAO;
import fr.inria.anhalytics.kb.entities.Monograph;
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
public class MonographDAO extends DAO<Monograph, Long> {

    private static final String SQL_INSERT
            = "INSERT INTO MONOGRAPH (type, title, shortname) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_MONOGR_BY_ID
            = "SELECT * FROM MONOGRAPH WHERE monographID = ?";

    public MonographDAO(Connection conn) {
        super(conn);
    }

    public boolean create(Monograph obj) {
        boolean result = false;
        if (obj.getMonographID() != null) {
            throw new IllegalArgumentException("Monograph is already created, the Monograph ID is not null.");
        }

        PreparedStatement statement;
        try {
            statement = connect.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, obj.getType());
            statement.setString(2, obj.getTitle());
            statement.setString(3, obj.getShortname());

            int code = statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();

            if (rs.next()) {
                obj.setMonographID(rs.getLong(1));
            }
            statement.close();
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(DocumentDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean delete(Monograph obj) {
        return false;
    }

    public boolean update(Monograph obj) {
        return false;
    }

    public Monograph find(Long monographID) {
        Monograph monograph = new Monograph();

        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(SQL_SELECT_MONOGR_BY_ID);
            //preparedStatement.setFetchSize(Integer.MIN_VALUE);
            preparedStatement.setLong(1, monographID);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                monograph = new Monograph(
                        monographID,
                        result.getString("type"),
                        result.getString("title"),
                        result.getString("shortname"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monograph;
    }
}
