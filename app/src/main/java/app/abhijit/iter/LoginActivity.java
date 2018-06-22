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

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private Cache mCache;
    private IterApi mIterApi;

    private AutoCompleteTextView mUsernameInput;
    private EditText mPasswordInput;
    private TextInputLayout mPasswordVisibility;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mCache = new Cache(mContext);
        mIterApi = new IterApi(mContext);

        mUsernameInput = findViewById(R.id.username);
        mPasswordInput = findViewById(R.id.password);
        mPasswordVisibility = findViewById(R.id.password_visibility);
        mLoginButton = findViewById(R.id.login);

        setupToolbar();
        setupUsernameInput();
        setupPasswordInput();
        setupLoginButton();

        Student selectedStudent = mCache.getStudent(mSharedPreferences.getString("pref_student", null));
        if (selectedStudent != null) {
            mUsernameInput.setText(selectedStudent.username);
            mPasswordInput.setText(selectedStudent.password);
            mPasswordVisibility.setPasswordVisibilityToggleEnabled(false);
            if (mSharedPreferences.getBoolean("pref_auto_login", true)) {
                mLoginButton.performClick();
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupUsernameInput() {
        final ArrayList<Student> students = mCache.getStudents();
        final ArrayList<String> usernames = new ArrayList<>();
        for (Student student : students) {
            usernames.add(student.username);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_dropdown_item_1line, usernames);
        mUsernameInput.setAdapter(adapter);
        mUsernameInput.setThreshold(1);

        mUsernameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int index = usernames.indexOf(mUsernameInput.getText().toString());
                mPasswordInput.setText(students.get(index).password);
                mPasswordVisibility.setPasswordVisibilityToggleEnabled(false);
            }
        });
    }

    private void setupPasswordInput() {
        mPasswordInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) mPasswordVisibility.setPasswordVisibilityToggleEnabled(true);
            }
        });
    }

    private void setupLoginButton() {
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String username = mUsernameInput.getText().toString();
                String password = mPasswordInput.getText().toString();

                if (username.isEmpty()) return;

                mSharedPreferences.edit().putString("pref_student", username).apply();

                mUsernameInput.setEnabled(false);
                mPasswordInput.setEnabled(false);
                mPasswordVisibility.setEnabled(false);
                mLoginButton.setEnabled(false);

                mLoginButton.setText("LOADING...");
                mLoginButton.setBackgroundResource(R.drawable.bg_login_button_loading);
                ((AnimationDrawable) mLoginButton.getBackground()).start();

                mIterApi.getStudent(username, password, new IterApi.Callback() {

                    @Override
                    public void onData(@NonNull final Student student) {
                        if (mCache.getStudent(student.username) == null) {
                            Toast.makeText(mContext, "Credentials will be stored on your device until you Logout", Toast.LENGTH_SHORT).show();
                        }
                        mLoginButton.setText(StringUtils.repeat(new String(Character.toChars(0x1F60F)), 3));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                                intent.putExtra("student", new Gson().toJson(student));
                                startActivity(intent);
                                finish();
                            }
                        }, 750);
                    }

                    @Override
                    public void onError(@NonNull RuntimeException error) {
                        if (error instanceof ConnectionFailedException) {
                            Toast.makeText(mContext, "ITER servers are currently down", Toast.LENGTH_LONG).show();
                        } else if (error instanceof InvalidCredentialsException) {
                            Toast.makeText(mContext, "Invalid credentials", Toast.LENGTH_LONG).show();
                        } else if (error instanceof InvalidResponseException) {
                            Toast.makeText(mContext, "Invalid API response", Toast.LENGTH_LONG).show();
                        }

                        if ((error instanceof InvalidResponseException || error instanceof ConnectionFailedException) &&
                                mCache.getStudent(mSharedPreferences.getString("pref_student", null)) != null) {
                                mLoginButton.setText("¯\\_(ツ)_/¯");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 750);
                        } else {
                            mSharedPreferences.edit().putString("pref_student", null).apply();

                            ((AnimationDrawable) mLoginButton.getBackground()).stop();
                            mUsernameInput.setEnabled(true);
                            mPasswordInput.setEnabled(true);
                            mPasswordVisibility.setEnabled(true);
                            mLoginButton.setBackgroundResource(R.drawable.bg_login_button_error);
                            mLoginButton.setText("ERROR");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mLoginButton.setEnabled(true);
                                    mLoginButton.setText("BUNK!");
                                    mLoginButton.setBackgroundResource(R.drawable.bg_login_button);
                                }
                            }, 2000);
                        }
                    }
                });
            }
        });
    }
}
