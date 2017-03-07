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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.common.collect.ImmutableList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import app.abhijit.iter.data.model.Error;
import app.abhijit.iter.data.model.Student;
import app.abhijit.iter.data.model.Subject;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    private StudentsAdapter mStudentsAdapter;
    private SubjectsAdapter mSubjectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        setupToolbar();
        setupDrawer();
        setupListAdapters();
        setupGettingStartedHint();
        setupAddStudentButton();

        if (!BuildConfig.DEBUG) {
            setupAd();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        ((MainApplication) getApplication()).getStudentDataSource().fetch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onStudentDataEvent(ImmutableList<Student> students) {
        reset();

        if (students.size() == 0) {
            displayGettingStartedHint(true);
            displayStudentsEmptyHint(true);
            return;
        }

        displayStudents(new ArrayList<>(students));

        Student selectedStudent = null;

        for (Student student : students) {
            if (student.isSelected()) {
                selectedStudent = student;
                break;
            }
        }

        if (selectedStudent == null) {
            displayGettingStartedHint(true);
            return;
        }

        if (selectedStudent.getName() == null) {
            displayLoading(true);
            return;
        }

        displayStudentDetail(selectedStudent);

        if (selectedStudent.getSubjects() == null) {
            displayLoading(true);
            return;
        }

        if (selectedStudent.getSubjects().size() == 0) {
            displaySubjectsEmptyHint(true);
            return;
        }

        displaySubjects(new ArrayList<>(selectedStudent.getSubjects()));

        for (Subject subject : selectedStudent.getSubjects()) {
            if (subject.getOldAttendance() != null) {
                vibrate();
                break;
            }
        }
    }

    @Subscribe
    public void onErrorDataEvent(Error error) {
        toastError(error.getMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            ((MainApplication) getApplication()).getStudentDataSource().refresh();
        } else if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        } else if (id == R.id.action_feedback) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_feedback))));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupListAdapters() {
        ListView studentsList = (ListView) findViewById(R.id.list_students);
        mStudentsAdapter = new StudentsAdapter(new ArrayList<Student>());
        studentsList.setAdapter(mStudentsAdapter);

        ListView subjectsList = (ListView) findViewById(R.id.list_subjects);
        mSubjectsAdapter = new SubjectsAdapter(new ArrayList<Subject>());
        subjectsList.setAdapter(mSubjectsAdapter);
    }

    private void setupGettingStartedHint() {
        TextView gettingStartedHint = (TextView) findViewById(R.id.hint_getting_started);
        int lineHeight = gettingStartedHint.getLineHeight();
        Drawable hamburgerIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_hamburger, null);
        hamburgerIcon.setBounds(0, 0, lineHeight, lineHeight);
        SpannableStringBuilder hintText = new SpannableStringBuilder();
        hintText.append(getResources().getString(R.string.hint_getting_started_1)).append(" ");
        hintText.setSpan(new ImageSpan(hamburgerIcon), hintText.length() - 1, hintText.length(), 0);
        hintText.append(getResources().getString(R.string.hint_getting_started_2));
        gettingStartedHint.setText(hintText);
    }

    private void setupAddStudentButton() {
        final Button addStudentButton = (Button) findViewById(R.id.student_add);
        addStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText addStudentEditText = (EditText) findViewById(R.id.student_registration_number_input);
                String registrationNumber = addStudentEditText.getText().toString();
                ((MainApplication) getApplication()).getStudentDataSource().select(registrationNumber);
                addStudentEditText.setText("");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus() == null ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawers();
            }
        });
    }

    private void setupAd() {
        MobileAds.initialize(mContext, getResources().getString(R.string.banner_ad_unit_id));
        AdView adView = (AdView) findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void displayUpdateAvailableDialog() {
        AlertDialog.Builder updateAvailableDialog = new AlertDialog.Builder(this);
        updateAvailableDialog.setTitle(getResources().getString(R.string.label_update_available));
        updateAvailableDialog.setMessage(getResources().getString(R.string.message_update_available));
        updateAvailableDialog.setPositiveButton(getResources().getString(R.string.label_update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_store))));
            }
        });
        updateAvailableDialog.create().show();
    }

    public void displayGettingStartedHint(boolean visible) {
        TextView gettingStartedHint = (TextView) findViewById(R.id.hint_getting_started);
        gettingStartedHint.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void displaySubjectsEmptyHint(boolean visible) {
        TextView subjectsEmptyHint = (TextView) findViewById(R.id.hint_subjects_empty);
        subjectsEmptyHint.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void displayStudentsEmptyHint(boolean visible) {
        TextView studentsEmptyHint = (TextView) findViewById(R.id.hint_students_empty);
        studentsEmptyHint.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void toastError(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void toastStudentDeleted(String registrationNumber) {
        Toast.makeText(mContext, getResources().getString(R.string.message_student_deleted, registrationNumber), Toast.LENGTH_SHORT).show();
    }

    public void displayLoading(boolean visible) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_indicator);
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void displayStudents(ArrayList<Student> students) {
        ListView studentsList = (ListView) findViewById(R.id.list_students);
        studentsList.setVisibility(students == null ? View.GONE : View.VISIBLE);

        if (students == null) return;

        Collections.sort(students, new Comparator<Student>() {
            @Override
            public int compare(Student lhs, Student rhs) {
                String lhsName = lhs.getName() == null ? lhs.getRegistrationNumber() : lhs.getName();
                String rhsName = rhs.getName() == null ? rhs.getRegistrationNumber() : rhs.getName();
                return lhsName.compareTo(rhsName);
            }
        });

        mStudentsAdapter.clear();
        mStudentsAdapter.addAll(students);
        mStudentsAdapter.notifyDataSetChanged();
    }

    public void displayStudentDetail(Student student) {
        getSupportActionBar().setTitle(student != null ? student.getName() : getResources().getString(R.string.app_name));
        getSupportActionBar().setSubtitle(student != null ? student.getRegistrationNumber() : null);
    }

    public void displaySubjects(ArrayList<Subject> subjects) {
        ListView subjectsList = (ListView) findViewById(R.id.list_subjects);
        subjectsList.setVisibility(subjects == null ? View.GONE : View.VISIBLE);

        if (subjects == null) return;

        Collections.sort(subjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject lhs, Subject rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        mSubjectsAdapter.clear();
        mSubjectsAdapter.addAll(subjects);
        mSubjectsAdapter.notifyDataSetChanged();
    }

    public void reset() {
        displayGettingStartedHint(false);
        displaySubjectsEmptyHint(false);
        displayStudentsEmptyHint(false);
        displayLoading(false);
        displayStudents(null);
        displayStudentDetail(null);
        displaySubjects(null);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private class StudentsAdapter extends ArrayAdapter<Student> {

        private final LayoutInflater mLayoutInflater;

        public StudentsAdapter(ArrayList<Student> students) {
            super(mContext, R.layout.item_student, students);
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        private class ViewHolder {
            private TextView name;
            private TextView registrationNumber;
            private ImageView delete;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Student student = getItem(position);
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item_student, parent, false);
                viewHolder.name = (TextView) convertView.findViewById(R.id.student_name);
                viewHolder.registrationNumber = (TextView) convertView.findViewById(R.id.student_registration_number);
                viewHolder.delete = (ImageView) convertView.findViewById(R.id.student_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (student.getName() == null) {
                viewHolder.name.setText(student.getRegistrationNumber());
                viewHolder.registrationNumber.setText("");
            } else {
                viewHolder.name.setText(student.getName());
                viewHolder.registrationNumber.setText(student.getRegistrationNumber());
            }
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainApplication) getApplication()).getStudentDataSource().delete(student.getRegistrationNumber());
                    toastStudentDeleted(student.getRegistrationNumber());
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainApplication) getApplication()).getStudentDataSource().select(student.getRegistrationNumber());
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawers();
                }
            });
            if (student.isSelected()) {
                convertView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.grey, null));
            } else {
                convertView.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null));
            }
            return convertView;
        }
    }

    private class SubjectsAdapter extends ArrayAdapter<Subject> {

        private final LayoutInflater mLayoutInflater;

        public SubjectsAdapter(ArrayList<Subject> subjects) {
            super(mContext, R.layout.item_subject, subjects);
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        private class ViewHolder {
            private ImageView avatar;
            private TextView name;
            private TextView oldAttendance;
            private TextView attendance;
            private ImageView status;
            private TextView lastUpdated;
            private TextView oldPresent;
            private TextView present;
            private TextView oldAbsent;
            private TextView absent;
            private TextView bunkStats;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Subject subject = getItem(position);
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item_subject, parent, false);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.subject_avatar);
                viewHolder.name = (TextView) convertView.findViewById(R.id.subject_name);
                viewHolder.oldAttendance = (TextView) convertView.findViewById(R.id.subject_old_attendance);
                viewHolder.attendance = (TextView) convertView.findViewById(R.id.subject_attendance);
                viewHolder.status = (ImageView) convertView.findViewById(R.id.subject_status);
                viewHolder.lastUpdated = (TextView) convertView.findViewById(R.id.subject_last_updated);
                viewHolder.oldPresent = (TextView) convertView.findViewById(R.id.subject_old_present);
                viewHolder.present = (TextView) convertView.findViewById(R.id.subject_present);
                viewHolder.oldAbsent = (TextView) convertView.findViewById(R.id.subject_old_absent);
                viewHolder.absent = (TextView) convertView.findViewById(R.id.subject_absent);
                viewHolder.bunkStats = (TextView) convertView.findViewById(R.id.subject_bunk_stats);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.avatar.setImageResource(subject.getAvatar());
            viewHolder.name.setText(subject.getName());
            if (subject.getOldAttendance() == null) {
                viewHolder.oldAttendance.setVisibility(View.GONE);
            } else {
                viewHolder.oldAttendance.setVisibility(View.VISIBLE);
                viewHolder.oldAttendance.setText(subject.getOldAttendance());
                viewHolder.oldAttendance.setPaintFlags(viewHolder.oldAttendance.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.attendance.setText(subject.getAttendance());
            viewHolder.status.setImageResource(subject.getStatus());
            viewHolder.lastUpdated.setText(subject.getLastUpdated());
            if (subject.getOldPresent() == null) {
                viewHolder.oldPresent.setVisibility(View.GONE);
            } else {
                viewHolder.oldPresent.setVisibility(View.VISIBLE);
                viewHolder.oldPresent.setText(subject.getOldPresent());
                viewHolder.oldPresent.setPaintFlags(viewHolder.oldPresent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.present.setText(subject.getPresent());
            if (subject.getOldAbsent() == null) {
                viewHolder.oldAbsent.setVisibility(View.GONE);
            } else {
                viewHolder.oldAbsent.setVisibility(View.VISIBLE);
                viewHolder.oldAbsent.setText(subject.getOldAbsent());
                viewHolder.oldAbsent.setPaintFlags(viewHolder.oldAbsent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.absent.setText(subject.getAbsent());
            if (subject.getBunkStats() == null) {
                viewHolder.bunkStats.setVisibility(View.GONE);
            } else {
                viewHolder.bunkStats.setVisibility(View.VISIBLE);
                viewHolder.bunkStats.setText(subject.getBunkStats());
            }

            return convertView;
        }
    }
}
