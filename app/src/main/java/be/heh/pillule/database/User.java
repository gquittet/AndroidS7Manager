package be.heh.pillule.database;

import java.util.ArrayList;

import be.heh.pillule.objects.Automate;

import static be.heh.pillule.security.Encrypt.hash;

/**
 * Created by gquittet on 12/4/17.
 */

public class User extends DBObject {

    private String lastname;
    private String firstname;
    private String email;
    private String password;
    private int roles;

    User() {
        super();
    }

    public User(String lastname, String firstname, String email, String password, int roles) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.email = email;
        this.password = hash(password);
        this.roles = roles;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoles() {
        return roles;
    }

    public void setRoles(int roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "Lastname: " + getLastname() + "\n" +
                "Firstname: " + getFirstname() + "\n" +
                "Email: " + getEmail() + "\n" +
                "Password: " + getPassword() + "\n" +
                "Roles:" + getRoles();
    }
}
