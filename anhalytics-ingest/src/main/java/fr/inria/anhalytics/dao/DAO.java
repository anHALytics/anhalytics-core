package fr.inria.anhalytics.dao;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DAO<T> {

    protected Connection connect = null;

    public DAO(Connection conn) {
        this.connect = conn;
    }

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean create(T obj) throws SQLException ;

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean delete(T obj) throws SQLException;

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean update(T obj) throws SQLException;

    /**
     * @param id
     * @return T
     */
    public abstract T find(Long id) throws SQLException;
}
