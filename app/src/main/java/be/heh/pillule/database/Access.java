package be.heh.pillule.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gquittet on 12/7/17.
 */

class Access<Table> {

    static final int VERSION = 1;
    static final String NOM_DB = "pillmanager.db";

    private SQLiteOpenHelper tableDb;
    private SQLiteDatabase db = null;

    void attachTable(Table table) {
        if (table == null) {
            throw new NullPointerException("The table can not be null.");
        }
        tableDb = (SQLiteOpenHelper) table;
    }

    public void openForWrite() {
        if (tableDb == null) {
            throw new NullPointerException("Attach a table to this db first.");
        }
        db = tableDb.getWritableDatabase();
    }

    public void openForRead() {
        if (tableDb == null) {
            throw new NullPointerException("Attach a table to this db first.");
        }
        db = tableDb.getReadableDatabase();
    }

    public SQLiteDatabase getDb() {
        if (db == null) {
            throw new NullPointerException("Open the database first!");
        }
        return db;
    }

    public void close() {
        db.close();
    }
}
