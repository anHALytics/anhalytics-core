package fr.inria.anhalytics.dao;

import java.sql.Connection;
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
    public abstract boolean create(E obj) throws SQLException ;

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
}
