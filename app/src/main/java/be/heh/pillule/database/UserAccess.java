package be.heh.pillule.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by gquittet on 12/7/17.
 */

public class UserAccess extends Access<UserTable> {

    public UserAccess(Context c) {
        UserTable userTable = new UserTable(c, NOM_DB, null, VERSION);
        super.attachTable(userTable);
    }
}
