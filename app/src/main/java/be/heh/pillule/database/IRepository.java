package be.heh.pillule.database;

import java.util.ArrayList;

/**
 * Created by gquittet on 12/7/17.
 */

interface IRepository<T> {

    long insert(T o);
    int update(int i, T o);
    int remove(String s);
    ArrayList<T> get(String query, String[] args);
    ArrayList<T> getAll();
}
