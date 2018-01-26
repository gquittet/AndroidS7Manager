package be.heh.pillule.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import be.heh.pillule.R;
import be.heh.pillule.database.User;
import be.heh.pillule.database.UserAccess;
import be.heh.pillule.database.UserRepository;
import be.heh.pillule.security.Admin;
import be.heh.pillule.security.Regex;

import static be.heh.pillule.security.Encrypt.hash;

public class EditUserActivity extends Activity {

    private Resources resources;

    private EditText et_edituser_firstname;
    private EditText et_edituser_lastname;
    private EditText et_edituser_email;
    private EditText et_edituser_password;
    private EditText et_edituser_password_confirm;
    private RadioButton rb_edituser_read;
    private RadioButton rb_edituser_write;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        resources = getApplicationContext().getResources();

        et_edituser_firstname = findViewById(R.id.et_edituser_firstname);
        et_edituser_lastname = findViewById(R.id.et_edituser_lastname);
        et_edituser_email = findViewById(R.id.et_edituser_email);
        et_edituser_password = findViewById(R.id.et_edituser_password);
        et_edituser_password_confirm = findViewById(R.id.et_edituser_password_confirm);
        rb_edituser_read = findViewById(R.id.rb_edituser_read);
        rb_edituser_write = findViewById(R.id.rb_edituser_write);

        int id = getIntent().getIntExtra("id", -1);

        UserAccess userDB = new UserAccess(this);
        userDB.openForRead();
        UserRepository userRepository = new UserRepository(userDB.getDb());
        user = userRepository.get("id = ?", new String[]{Integer.toString(id)}).get(0);
        userDB.close();

        if (user.getEmail().equals(Admin.getUsername())) {
            et_edituser_firstname.setEnabled(false);
            et_edituser_firstname.setFocusable(false);
            et_edituser_firstname.setVisibility(View.INVISIBLE);
            et_edituser_lastname.setEnabled(false);
            et_edituser_lastname.setFocusable(false);
            et_edituser_lastname.setVisibility(View.INVISIBLE);
            et_edituser_email.setEnabled(false);
            et_edituser_email.setFocusable(false);
            et_edituser_email.setText(user.getEmail());
            et_edituser_email.setTextColor(Color.BLACK);
            rb_edituser_read.setEnabled(false);
            rb_edituser_read.setFocusable(false);
            rb_edituser_write.setEnabled(false);
            rb_edituser_write.setFocusable(false);
        } else {
            et_edituser_firstname.setText(user.getFirstname());
            et_edituser_lastname.setText(user.getLastname());
            et_edituser_email.setText(user.getEmail());
            rb_edituser_read.setChecked(user.getRoles() == 0);
            rb_edituser_write.setChecked(user.getRoles() == 1);
        }
    }

    public void onEditUserClickManager(View v) {
        switch (v.getId()) {
            case R.id.btn_edituser_edit:
                String lastname = et_edituser_lastname.getText().toString();
                String firstname = et_edituser_firstname.getText().toString();
                String email = et_edituser_email.getText().toString();
                String password = et_edituser_password.getText().toString();
                String passwordConfirm = et_edituser_password_confirm.getText().toString();
                int role = rb_edituser_read.isChecked() ? 0 : 1;
                boolean error = false;
                if (!user.getEmail().equals(Admin.getUsername())) {
                    if (!Regex.isName(lastname)) {
                        et_edituser_lastname.setError(resources.getString(R.string.errNameInvalid));
                        error = true;
                    }
                    if (!Regex.isName(firstname)) {
                        et_edituser_firstname.setError(resources.getString(R.string.errNameInvalid));
                        error = true;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        et_edituser_email.setError(resources.getString(R.string.errEmailInvalid));
                        error = true;
                    }
                }
                if (!password.equals("") && !Regex.isPassword(password)) {
                    et_edituser_password.setError(resources.getString(R.string.errPasswordInvalid));
                    error = true;
                }
                if (!password.equals("") && !passwordConfirm.equals("") && !password.equals(passwordConfirm)) {
                    et_edituser_password_confirm.setError(resources.getString(R.string.errPasswordMismatch));
                    error = true;
                }
                if (!passwordConfirm.equals("") && !Regex.isPassword(passwordConfirm)) {
                    et_edituser_password_confirm.setError(resources.getString(R.string.errPasswordInvalid));
                    error = true;
                }
                if (error)
                    break;
                user.setLastname(lastname);
                user.setFirstname(firstname);
                user.setEmail(email);
                if (!password.equals("") && !passwordConfirm.equals("")) {
                    user.setPassword(hash(password));
                }
                user.setRoles(role);
                UserAccess userDB = new UserAccess(this);
                userDB.openForWrite();
                UserRepository userRepository = new UserRepository(userDB.getDb());
                long result = userRepository.update(user.getId(), user);
                if (result == -1) {
                    String errorMessage = getApplicationContext().getResources().getString(R.string.errEmailAlreadyExist);
                    et_edituser_email.setError(errorMessage);
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                userDB.close();
                setResult(RESULT_OK);
                finish();
                break;

        }
    }
}
