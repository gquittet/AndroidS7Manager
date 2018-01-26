package be.heh.pillule.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import be.heh.pillule.R;
import be.heh.pillule.database.User;
import be.heh.pillule.database.UserAccess;
import be.heh.pillule.database.UserRepository;
import be.heh.pillule.security.Regex;

public class SignUpActivity extends Activity {

    private Resources resources;

    private EditText et_signup_firstname;
    private EditText et_signup_lastname;
    private EditText et_signup_email;
    private EditText et_signup_password;
    private EditText et_signup_password_confirm;
    private Button btn_signup_signup;
    private Button btn_signup_signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        resources = getApplicationContext().getResources();

        et_signup_firstname = findViewById(R.id.et_signup_firstname);
        et_signup_lastname = findViewById(R.id.et_signup_lastname);
        et_signup_email = findViewById(R.id.et_signup_email);
        et_signup_password = findViewById(R.id.et_signup_password);
        et_signup_password_confirm = findViewById(R.id.et_signup_password_confirm);
        btn_signup_signup = findViewById(R.id.btn_signup_signup);
        btn_signup_signin = findViewById(R.id.btn_signup_signin);
    }

    public void onClickManager(View v) {
        switch (v.getId()) {
            case R.id.btn_signup_signup:
                String lastname = et_signup_lastname.getText().toString();
                String firstname = et_signup_firstname.getText().toString();
                String email = et_signup_email.getText().toString();
                String password = et_signup_password.getText().toString();
                String passwordConfirm = et_signup_password_confirm.getText().toString();
                boolean error = false;
                if (!Regex.isName(lastname)) {
                    et_signup_lastname.setError(resources.getString(R.string.errNameInvalid));
                    error = true;
                }
                if (!Regex.isName(firstname)) {
                    et_signup_firstname.setError(resources.getString(R.string.errNameInvalid));
                    error = true;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    et_signup_email.setError(resources.getString(R.string.errEmailInvalid));
                    error = true;
                }
                if (!Regex.isPassword(password)) {
                    et_signup_password.setError(resources.getString(R.string.errPasswordInvalid));
                    error = true;
                }
                if (!password.equals(passwordConfirm)) {
                    et_signup_password_confirm.setError(resources.getString(R.string.errPasswordMismatch));
                    error = true;
                }
                if (!Regex.isPassword(passwordConfirm)) {
                    et_signup_password_confirm.setError(resources.getString(R.string.errPasswordInvalid));
                    error = true;
                }
                if (error)
                    break;
                User user = new User(lastname, firstname, email, password, 0);
                UserAccess userDB = new UserAccess(this);
                userDB.openForWrite();
                UserRepository userRepository = new UserRepository(userDB.getDb());
                long result = userRepository.insert(user);
                if (result == -1) {
                    String errorMessage = getApplicationContext().getResources().getString(R.string.errEmailAlreadyExist);
                    et_signup_email.setError(errorMessage);
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                userDB.close();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.btn_signup_signin:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

}
