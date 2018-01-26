package be.heh.pillule.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
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
import be.heh.pillule.security.Admin;
import be.heh.pillule.security.Regex;

import static be.heh.pillule.security.Encrypt.hash;

public class SignInActivity extends Activity {

    private Resources resources;

    private EditText et_signin_email;
    private EditText et_signin_password;
    private Button btn_signin_signin;
    private Button btn_signin_signup;

    private final int ACTIVITY_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        resources = getApplicationContext().getResources();

        et_signin_email = findViewById(R.id.et_signin_email);
        et_signin_password = findViewById(R.id.et_signin_password);
        btn_signin_signin = findViewById(R.id.btn_signin_signin);
        btn_signin_signup = findViewById(R.id.btn_signin_signup);

        UserAccess userDb = new UserAccess(this);
        userDb.openForWrite();
        UserRepository userRepository = new UserRepository(userDb.getDb());
        ArrayList<User> userList = userRepository.get("email = ?",
                new String[]{Admin.getUsername()});
        if (userList.size() == 0) {
            User admin = new User("", "", Admin.getUsername(), "", 3);
            admin.setPassword(Admin.getPassword());
            userRepository.insert(admin);
        } else {
            Admin.setUsername(userList.get(0).getEmail());
            Admin.setPassword(userList.get(0).getPassword());
        }
        userDb.close();
    }

    public void onClickManager(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_signin_signin:
                String email = et_signin_email.getText().toString();
                String password = et_signin_password.getText().toString();
                if (email.equals(Admin.getUsername()) && hash(password).equals(Admin.getPassword())) {
                    intent = new Intent(getApplicationContext(), AdminActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
                boolean error = false;
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    et_signin_email.setError(resources.getString(R.string.errEmailInvalid));
                    error = true;
                }
                if (!Regex.isPassword(password)) {
                    et_signin_password.setError(resources.getString(R.string.errPasswordInvalid));
                    error = true;
                }
                if (error)
                    break;
                UserAccess userDb = new UserAccess(this);
                userDb.openForRead();
                UserRepository userRepository = new UserRepository(userDb.getDb());
                ArrayList<User> userList = userRepository.get("email = ? and password = ?",
                        new String[]{email, hash(password)});
                if (userList.size() > 0) {
                    intent = new Intent(getApplicationContext(), UserActivity.class);
                    intent.putExtra("canWrite", userList.get(0).getRoles());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.errUserNotFound, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_signin_signup:
                intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, ACTIVITY_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        Toast.makeText(getApplicationContext(),
                                resources.getString(R.string.stateSignUpSuccessful),
                                Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
