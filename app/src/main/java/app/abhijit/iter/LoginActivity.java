package app.abhijit.iter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import app.abhijit.iter.data.Cache;
import app.abhijit.iter.data.IterApi;
import app.abhijit.iter.exceptions.ConnectionFailedException;
import app.abhijit.iter.exceptions.InvalidCredentialsException;
import app.abhijit.iter.exceptions.InvalidResponseException;
import app.abhijit.iter.models.Student;

public class LoginActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences sharedPreferences;
    private Cache cache;
    private IterApi iterApi;

    private AutoCompleteTextView usernameInput;
    private EditText passwordInput;
    private TextInputLayout passwordVisibility;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.context = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.cache = new Cache(this.context);
        this.iterApi = new IterApi(this.context);

        this.usernameInput = findViewById(R.id.username);
        this.passwordInput = findViewById(R.id.password);
        this.passwordVisibility = findViewById(R.id.password_visibility);
        this.loginButton = findViewById(R.id.login);

        setupToolbar();
        setupUsernameInput();
        setupPasswordInput();
        setupLoginButton();

        Student selectedStudent = this.cache.getStudent(this.sharedPreferences.getString("pref_student", null));
        if (selectedStudent != null) {
            this.usernameInput.setText(selectedStudent.getUsername());
            this.passwordInput.setText(selectedStudent.getPassword());
            this.passwordVisibility.setPasswordVisibilityToggleEnabled(false);
            if (this.sharedPreferences.getBoolean("pref_auto_login", true)) {
                this.loginButton.performClick();
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupUsernameInput() {
        final ArrayList<Student> students = this.cache.getStudents();
        final ArrayList<String> usernames = new ArrayList<>();
        for (Student student : students) {
            usernames.add(student.getUsername());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.context,
                android.R.layout.simple_dropdown_item_1line, usernames);
        this.usernameInput.setAdapter(adapter);
        this.usernameInput.setThreshold(1);

        this.usernameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int index = usernames.indexOf(LoginActivity.this.usernameInput.getText().toString());
                LoginActivity.this.passwordInput.setText(students.get(index).getPassword());
                LoginActivity.this.passwordVisibility.setPasswordVisibilityToggleEnabled(false);
            }
        });
    }

    private void setupPasswordInput() {
        this.passwordInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) passwordVisibility.setPasswordVisibilityToggleEnabled(true);
            }
        });
    }

    private void setupLoginButton() {
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String username = LoginActivity.this.usernameInput.getText().toString();
                String password = LoginActivity.this.passwordInput.getText().toString();

                if (username.isEmpty()) return;

                LoginActivity.this.sharedPreferences.edit().putString("pref_student", username).apply();

                LoginActivity.this.usernameInput.setEnabled(false);
                LoginActivity.this.passwordInput.setEnabled(false);
                LoginActivity.this.passwordVisibility.setEnabled(false);
                LoginActivity.this.loginButton.setEnabled(false);

                LoginActivity.this.loginButton.setText("LOADING...");
                LoginActivity.this.loginButton.setBackgroundResource(R.drawable.bg_login_button_loading);
                ((AnimationDrawable) LoginActivity.this.loginButton.getBackground()).start();

                LoginActivity.this.iterApi.getStudent(username, password, new IterApi.Callback() {

                    @Override
                    public void onData(@NonNull final Student student) {
                        if (LoginActivity.this.cache.getStudent(student.getUsername()) == null) {
                            Toast.makeText(LoginActivity.this.context, "Credentials will be stored on your device until you Logout", Toast.LENGTH_SHORT).show();
                        }
                        LoginActivity.this.loginButton.setText(StringUtils.repeat(new String(Character.toChars(0x1F60F)), 3));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                                intent.putExtra("student", new Gson().toJson(student));
                                startActivity(intent);
                                finish();
                            }
                        }, 400);
                    }

                    @Override
                    public void onError(@NonNull RuntimeException e) {
                        // TODO: refactor exception handling

                        if (e instanceof ConnectionFailedException || e instanceof InvalidCredentialsException
                                || e instanceof InvalidResponseException) {
                            Toast.makeText(LoginActivity.this.context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if ((e instanceof InvalidResponseException || e instanceof ConnectionFailedException) &&
                                LoginActivity.this.cache.getStudent(LoginActivity.this.sharedPreferences.getString("pref_student", null)) != null) {
                                LoginActivity.this.loginButton.setText("¯\\_(ツ)_/¯");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 400);
                        } else {
                            LoginActivity.this.sharedPreferences.edit().putString("pref_student", null).apply();

                            ((AnimationDrawable) LoginActivity.this.loginButton.getBackground()).stop();
                            LoginActivity.this.usernameInput.setEnabled(true);
                            LoginActivity.this.passwordInput.setEnabled(true);
                            LoginActivity.this.passwordVisibility.setEnabled(true);
                            LoginActivity.this.loginButton.setBackgroundResource(R.drawable.bg_login_button_error);
                            LoginActivity.this.loginButton.setText("ERROR");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LoginActivity.this.loginButton.setEnabled(true);
                                    LoginActivity.this.loginButton.setText("BUNK!");
                                    LoginActivity.this.loginButton.setBackgroundResource(R.drawable.bg_login_button);
                                }
                            }, 1200);
                        }
                    }
                });
            }
        });
    }
}
