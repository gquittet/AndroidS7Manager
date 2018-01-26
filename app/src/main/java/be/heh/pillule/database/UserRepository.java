package be.heh.pillule.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by gquittet on 12/7/17.
 */

public class UserRepository implements IRepository<User> {

    private static final String TABLE_USER = "user";
    private static final String COL_ID = "id";
    private static final int NUM_COL_ID = 0;
    private static final String COL_LASTNAME = "lastname";
    private static final int NUM_COL_LASTNAME = 1;
    private static final String COL_FIRSTNAME = "firstname";
    private static final int NUM_COL_FIRSTNAME = 2;
    private static final String COL_EMAIL = "email";
    private static final int NUM_COL_EMAIL = 3;
    private static final String COL_PASSWORD = "password";
    private static final int NUM_COL_PASSWORD = 4;
    private static final String COL_ROLES = "roles";
    private static final int NUM_COL_ROLES = 5;

    private final SQLiteDatabase db;

    public UserRepository(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public long insert(User u) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_LASTNAME, u.getLastname());
        contentValues.put(COL_FIRSTNAME, u.getFirstname());
        contentValues.put(COL_EMAIL, u.getEmail());
        contentValues.put(COL_PASSWORD, u.getPassword());
        contentValues.put(COL_ROLES, u.getRoles());
        return db.insert(TABLE_USER, null, contentValues);
    }

    @Override
    public int update(int i, User u) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_LASTNAME, u.getLastname());
        contentValues.put(COL_FIRSTNAME, u.getFirstname());
        contentValues.put(COL_EMAIL, u.getEmail());
        contentValues.put(COL_PASSWORD, u.getPassword());
        contentValues.put(COL_ROLES, u.getRoles());
        return db.update(TABLE_USER, contentValues, COL_ID + " = ?", new String[]{Integer.toString(i)});
    }

    @Override
    public int remove(String email) {
        return db.delete(TABLE_USER, COL_EMAIL + " = ?", new String[]{email});
    }

    @Override
    public ArrayList<User> get(String query, String[] args) {
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + query, args);

        ArrayList<User> tabUser = new ArrayList<>();

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                User user1 = new User();
                user1.setId(c.getInt(NUM_COL_ID));
                user1.setLastname(c.getString(NUM_COL_LASTNAME));
                user1.setFirstname(c.getString(NUM_COL_FIRSTNAME));
                user1.setEmail(c.getString(NUM_COL_EMAIL));
                user1.setPassword(c.getString(NUM_COL_PASSWORD));
                user1.setRoles(c.getInt(NUM_COL_ROLES));
                tabUser.add(user1);
            }
        }

        c.close();
        return tabUser;
    }


    @Override
    public ArrayList<User> getAll() {
        Cursor c = db.query(
                TABLE_USER,
                new String[]{
                        COL_ID,
                        COL_LASTNAME,
                        COL_FIRSTNAME,
                        COL_EMAIL,
                        COL_PASSWORD,
                        COL_ROLES
                }, null, null, null, null,
                COL_EMAIL);

        ArrayList<User> tabUser = new ArrayList<>();

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                User user1 = new User();
                user1.setId(c.getInt(NUM_COL_ID));
                user1.setLastname(c.getString(NUM_COL_LASTNAME));
                user1.setFirstname(c.getString(NUM_COL_FIRSTNAME));
                user1.setEmail(c.getString(NUM_COL_EMAIL));
                user1.setPassword(c.getString(NUM_COL_PASSWORD));
                user1.setRoles(c.getInt(NUM_COL_ROLES));
                tabUser.add(user1);
            }
        }

        c.close();
        return tabUser;

    }
}
