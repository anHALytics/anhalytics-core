package fr.inria.anhalytics.commons.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DAO<E, T> {

    protected Connection connect = null;

    public DAO(Connection conn) {
        this.connect = conn;
    }

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean create(E obj) throws SQLException;

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean delete(E obj) throws SQLException;

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean update(E obj) throws SQLException;

    /**
     * @param id
     * @return T
     */
    public abstract E find(T id) throws SQLException;

    public static void closeQuietly(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException se) {
                //ignoring
            }
        }
    }
}
