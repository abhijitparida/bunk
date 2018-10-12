/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Abhijit Parida <abhijitparida.me@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        cache = new Cache(context);
        iterApi = new IterApi(context);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        passwordVisibility = findViewById(R.id.password_visibility);
        loginButton = findViewById(R.id.login);

        setupToolbar();
        setupUsernameInput();
        setupPasswordInput();
        setupLoginButton();

        Student selectedStudent = cache.getStudent(mSharedPreferences.getString("pref_student", null));
        if (selectedStudent != null) {
            usernameInput.setText(selectedStudent.username);
            passwordInput.setText(selectedStudent.password);
            passwordVisibility.setPasswordVisibilityToggleEnabled(false);
            if (sharedPreferences.getBoolean("pref_auto_login", true)) {
                loginButton.performClick();
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupUsernameInput() {
        final ArrayList<Student> students = cache.getStudents();
        final ArrayList<String> usernames = new ArrayList<>();
        for (Student student : students) {
            usernames.add(student.username);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, usernames);
        usernameInput.setAdapter(adapter);
        usernameInput.setThreshold(1);

        usernameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int index = usernames.indexOf(usernameInput.getText().toString());
                passwordInput.setText(students.get(index).password);
                passwordVisibility.setPasswordVisibilityToggleEnabled(false);
            }
        });
    }

    private void setupPasswordInput() {
        passwordInput.addTextChangedListener(new TextWatcher() {

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
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();

                if (username.isEmpty()) return;

                sharedPreferences.edit().putString("pref_student", username).apply();

                usernameInput.setEnabled(false);
                passwordInput.setEnabled(false);
                passwordVisibility.setEnabled(false);
                loginButton.setEnabled(false);

                loginButton.setText("LOADING...");
                loginButton.setBackgroundResource(R.drawable.bg_login_button_loading);
                ((AnimationDrawable) loginButton.getBackground()).start();

                iterApi.getStudent(username, password, new IterApi.Callback() {

                    @Override
                    public void onData(@NonNull final Student student) {
                        if (cache.getStudent(student.username) == null) {
                            Toast.makeText(context, "Credentials will be stored on your device until you Logout", Toast.LENGTH_SHORT).show();
                        }
                        loginButton.setText(StringUtils.repeat(new String(Character.toChars(0x1F60F)), 3));
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
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if ((e instanceof InvalidResponseException || e instanceof ConnectionFailedException) &&
                                cache.getStudent(mSharedPreferences.getString("pref_student", null)) != null) {
                                loginButton.setText("¯\\_(ツ)_/¯");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 400);
                        } else {
                            sharedPreferences.edit().putString("pref_student", null).apply();

                            ((AnimationDrawable) mLoginButton.getBackground()).stop();
                            usernameInput.setEnabled(true);
                            passwordInput.setEnabled(true);
                            passwordVisibility.setEnabled(true);
                            loginButton.setBackgroundResource(R.drawable.bg_login_button_error);
                            loginButton.setText("ERROR");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginButton.setEnabled(true);
                                    loginButton.setText("BUNK!");
                                    loginButton.setBackgroundResource(R.drawable.bg_login_button);
                                }
                            }, 1200);
                        }
                    }
                });
            }
        });
    }
}
