package fr.inria.anhalytics.dao;

import java.sql.Connection;

public abstract class DAO<T> {

    protected Connection connect = null;

    public DAO(Connection conn) {
        this.connect = conn;
    }

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean create(T obj);

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean delete(T obj);

    /**
     * @param obj
     * @return boolean
     */
    public abstract boolean update(T obj);

    /**
     * @param id
     * @return T
     */
    public abstract T find(Long id);
}
