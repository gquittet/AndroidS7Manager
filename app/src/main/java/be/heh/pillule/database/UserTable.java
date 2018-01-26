package be.heh.pillule.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gquittet on 12/4/17.
 */

class UserTable extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "user";
    private static final String COL_ID = "id";
    private static final String COL_LASTNAME = "lastname";
    private static final String COL_FIRSTNAME = "firstname";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_ROLES = "roles";


    UserTable(Context context,
              String name,
              SQLiteDatabase.CursorFactory factory,
              int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_DB = "CREATE TABLE " + TABLE_NAME + "(" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_LASTNAME + " VARCHAR(55) NOT NULL, " +
                COL_FIRSTNAME + " VARCHAR(55) NOT NULL, " +
                COL_EMAIL + " VARCHAR(255) UNIQUE NOT NULL, " +
                COL_PASSWORD + " VARCHAR(255) NOT NULL, " +
                COL_ROLES + " INTEGER NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
