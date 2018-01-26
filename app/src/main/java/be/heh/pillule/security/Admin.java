package be.heh.pillule.security;

import static be.heh.pillule.security.Encrypt.hash;

/**
 * Created by gquittet on 12/8/17.
 */

public class Admin {

    private static String username = "android";
    private static String password = hash("android3");

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Admin.username = username;
    }

    public static void setPassword(String password) {
        Admin.password = password;
    }

    public static String getPassword() {
        return password;
    }
}
