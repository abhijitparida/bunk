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
import android.graphics.Paint;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import app.abhijit.iter.data.Cache;
import app.abhijit.iter.models.Student;
import app.abhijit.iter.models.Subject;

public class AttendanceActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private Context context;
    private SharedPreferences sharedPreferences;
    private Cache Cache;
    private Student NewStudent;
    private Student oldStudent;
    private boolean PrefExtendedStats;
    private int PrefMinimumAttendance;
    private ArrayList<SubjectView> SubjectViews;
    private SubjectAdapter SubjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        this.context = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.Cache = new Cache(this.context);

        try {
            this.NewStudent = new Gson().fromJson(getIntent().getStringExtra("student"), Student.class);
        } catch (Exception ignored) { }
        this.oldStudent = this.Cache.getStudent(this.sharedPreferences.getString("pref_student", null));
        if (this.NewStudent == null && this.oldStudent == null) {
            startActivity(new Intent(AttendanceActivity.this, LoginActivity.class));
            finish();
        }
        if (this.NewStudent == null) this.NewStudent = this.oldStudent;
        if (this.oldStudent == null) this.oldStudent = this.NewStudent;

        this.PrefExtendedStats = this.sharedPreferences.getBoolean("pref_extended_stats", true);
        this.PrefMinimumAttendance = Integer.parseInt(this.sharedPreferences.getString("pref_minimum_attendance", "75"));

        setupToolbar();
        setupDrawer();
        setupFab();
        setupListView();

        processAndDisplayAttendance();
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean prefExtendedStats = this.sharedPreferences.getBoolean("pref_extended_stats", true);
        int prefMinimumAttendance = Integer.parseInt(this.sharedPreferences.getString("pref_minimum_attendance", "75"));

        if (this.PrefExtendedStats != prefExtendedStats
                || this.PrefMinimumAttendance != prefMinimumAttendance) {
            this.PrefExtendedStats = prefExtendedStats;
            this.PrefMinimumAttendance = prefMinimumAttendance;

            processAndDisplayAttendance();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.three_dots, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView imageView = (ImageView) inflater.inflate(R.layout.action_refresh, null);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
            animation.setRepeatCount(1);
            imageView.startAnimation(animation);
            item.setActionView(imageView);
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    item.getActionView().clearAnimation();
                    item.setActionView(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        } else if (id == R.id.action_logout) {
            this.Cache.setStudent(this.sharedPreferences.getString("pref_student", null), null);
            Toast.makeText(this.context, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AttendanceActivity.this, LoginActivity.class));
            finish();
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) {
            startActivity(new Intent(AttendanceActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(AttendanceActivity.this, SettingsActivity.class));
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationViewHeader = navigationView.getHeaderView(0);
        ((TextView) navigationViewHeader.findViewById(R.id.name)).setText(this.NewStudent.name);
        ((TextView) navigationViewHeader.findViewById(R.id.username)).setText(this.NewStudent.username);
        String prompts[] = {"open source?", "coding?", "programming?", "code+coffee?"};
        TextView opensource = drawer.findViewById(R.id.opensource);
        opensource.setText(prompts[new Random().nextInt(prompts.length)]);
        TextView github = drawer.findViewById(R.id.github);
        github.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.github_url))));
            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AttendanceActivity.this.sharedPreferences.edit().putString("pref_student", null).apply();
                startActivity(new Intent(AttendanceActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void setupListView() {
        ListView subjectsList = findViewById(R.id.subjects);
        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        subjectsList.addFooterView(layoutInflater.inflate(R.layout.listview_footer, null, false));
        this.SubjectViews = new ArrayList<>();
        this.SubjectAdapter = new SubjectAdapter(this.SubjectViews);
        subjectsList.setAdapter(this.SubjectAdapter);
    }

    private void processAndDisplayAttendance() {
        ArrayList<SubjectView> subjectViews = new ArrayList<>();
        Boolean updated = false;
        for (Subject subject : this.NewStudent.subjects.values()) {
            SubjectView subjectView = new SubjectView();
            subjectView.avatar = subjectAvatar(subject.code);
            subjectView.name = subject.name;
            subjectView.attendance = String.format(Locale.US, "%.0f%%", Math.floor(subject.attendance()));
            subjectView.theory = String.format(Locale.US,
                    subject.theoryTotal == 1 ? "%d/%d class" : "%d/%d classes",
                    subject.theoryPresent, subject.theoryTotal);
            subjectView.lab = String.format(Locale.US,
                    subject.labTotal == 1 ? "%d/%d class" : "%d/%d classes",
                    subject.labPresent, subject.labTotal);
            subjectView.absent = String.format(Locale.US,
                    subject.absent() == 1 ? "%d class" : "%d classes",
                    subject.absent());
            if (this.oldStudent.subjects.containsKey(subject.code)) {
                Subject oldSubject = this.oldStudent.subjects.get(subject.code);
                if (subject.theoryPresent != oldSubject.theoryPresent
                        || subject.theoryTotal != oldSubject.theoryTotal
                        || subject.labPresent != oldSubject.labPresent
                        || subject.labTotal != oldSubject.labTotal) {
                    updated = true;
                    subjectView.oldTheory = String.format(Locale.US,
                            oldSubject.theoryTotal == 1 ? "%d/%d class" : "%d/%d classes",
                            oldSubject.theoryPresent, oldSubject.theoryTotal);
                    subjectView.oldLab = String.format(Locale.US,
                            oldSubject.labTotal == 1 ? "%d/%d class" : "%d/%d classes",
                            oldSubject.labPresent, oldSubject.labTotal);
                    subjectView.oldAbsent = String.format(Locale.US,
                            oldSubject.absent() == 1 ? "%d class" : "%d classes",
                            oldSubject.absent());
                    subjectView.updated = true;
                    if (subject.attendance() >= oldSubject.attendance()) {
                        subjectView.status = R.drawable.ic_status_up;
                    } else {
                        subjectView.status = R.drawable.ic_status_down;
                    }
                } else {
                    subject.lastUpdated = oldSubject.lastUpdated;
                }
            }
            if (subject.attendance() >= this.PrefMinimumAttendance + 10.0) {
                subjectView.attendanceBadge = R.drawable.bg_badge_green;
            } else if (subject.attendance() >= this.PrefMinimumAttendance) {
                subjectView.attendanceBadge = R.drawable.bg_badge_yellow;
            } else {
                subjectView.attendanceBadge = R.drawable.bg_badge_red;
            }
            subjectView.bunkStats = subject.bunkStats(this.PrefMinimumAttendance, this.PrefExtendedStats);
            if (subjectView.updated) {
                subjectView.lastUpdated = "just now";
            } else {
                CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(subject.lastUpdated, new Date().getTime(), 0);
                subjectView.lastUpdated = timeSpan.toString().toLowerCase();
            }
            subjectViews.add(subjectView);
        }
        this.Cache.setStudent(this.NewStudent.username, this.NewStudent);

        if (updated) {
            Toast.makeText(this.context, "Attendance updated", Toast.LENGTH_SHORT).show();
            Vibrator vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }

        findViewById(R.id.no_attendance).setVisibility(subjectViews.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.subjects).setVisibility(subjectViews.isEmpty() ? View.GONE : View.VISIBLE);

        this.SubjectViews.clear();
        this.SubjectViews.addAll(subjectViews);
        this.SubjectAdapter.notifyDataSetChanged();
    }

    private int subjectAvatar(String subjectCode) {
        switch (subjectCode) {
            case "CHM1002": return R.drawable.ic_subject_environmental_studies;
            case "CSE1002": return R.drawable.ic_subject_maths;
            case "CSE1011": return R.drawable.ic_subject_electrical;
            case "CSE2031": return R.drawable.ic_subject_maths;
            case "CSE3151": return R.drawable.ic_subject_database;
            case "CSE4042": return R.drawable.ic_subject_network;
            case "CSE4043": return R.drawable.ic_subject_security;
            case "CSE4044": return R.drawable.ic_subject_security;
            case "CSE4051": return R.drawable.ic_subject_database;
            case "CSE4052": return R.drawable.ic_subject_database;
            case "CSE4053": return R.drawable.ic_subject_database;
            case "CSE4054": return R.drawable.ic_subject_database;
            case "CSE4102": return R.drawable.ic_subject_html;
            case "CSE4141": return R.drawable.ic_subject_android;
            case "CSE4151": return R.drawable.ic_subject_server;
            case "CVL3071": return R.drawable.ic_subject_traffic;
            case "CVL3241": return R.drawable.ic_subject_water;
            case "CVL4031": return R.drawable.ic_subject_earth;
            case "CVL4032": return R.drawable.ic_subject_soil;
            case "CVL4041": return R.drawable.ic_subject_water;
            case "CVL4042": return R.drawable.ic_subject_water;
            case "EET1001": return R.drawable.ic_subject_matlab;
            case "EET3041": return R.drawable.ic_subject_electromagnetic_waves;
            case "EET3061": return R.drawable.ic_subject_communication;
            case "EET3062": return R.drawable.ic_subject_communication;
            case "EET4014": return R.drawable.ic_subject_renewable_energy;
            case "EET4041": return R.drawable.ic_subject_electromagnetic_waves;
            case "EET4061": return R.drawable.ic_subject_wifi;
            case "EET4063": return R.drawable.ic_subject_communication;
            case "EET4161": return R.drawable.ic_subject_communication;
            case "HSS1001": return R.drawable.ic_subject_effective_speech;
            case "HSS1021": return R.drawable.ic_subject_economics;
            case "HSS2021": return R.drawable.ic_subject_economics;
            case "MEL3211": return R.drawable.ic_subject_water;
            case "MTH2002": return R.drawable.ic_subject_probability_statistics;
            case "MTH4002": return R.drawable.ic_subject_matlab;
        }

        switch (subjectCode.substring(0, Math.min(subjectCode.length(), 3))) {
            case "CHM": return R.drawable.ic_subject_chemistry;
            case "CSE": return R.drawable.ic_subject_computer;
            case "CVL": return R.drawable.ic_subject_civil;
            case "EET": return R.drawable.ic_subject_electrical;
            case "HSS": return R.drawable.ic_subject_humanities;
            case "MEL": return R.drawable.ic_subject_mechanical;
            case "MTH": return R.drawable.ic_subject_maths;
            case "PHY": return R.drawable.ic_subject_physics;
        }

        return R.drawable.ic_subject_generic;
    }

    private class SubjectView {

        private boolean updated;
        private int avatar;
        private String attendance;
        private int attendanceBadge;
        private String name;
        private String lastUpdated;
        private int status;
        private String oldLab;
        private String lab;
        private String oldTheory;
        private String theory;
        private String oldAbsent;
        private String absent;
        private String bunkStats;
    }

    private class SubjectAdapter extends ArrayAdapter<SubjectView> {

        private final LayoutInflater layoutInflater;

        SubjectAdapter(ArrayList<SubjectView> subjectViews) {
            super(AttendanceActivity.this.context, R.layout.item_subject, subjectViews);

            this.layoutInflater = LayoutInflater.from(AttendanceActivity.this.context);
        }

        private class ViewHolder {

            private ImageView avatar;
            private TextView attendance;
            private TextView name;
            private TextView lastUpdated;
            private ImageView status;
            private TextView labLabel;
            private TextView oldLab;
            private TextView lab;
            private TextView theoryLabel;
            private TextView oldTheory;
            private TextView theory;
            private TextView oldAbsent;
            private TextView absent;
            private TextView bunkStats;
        }

        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.item_subject, parent, false);
                viewHolder.avatar = convertView.findViewById(R.id.subject_avatar);
                viewHolder.attendance = convertView.findViewById(R.id.subject_attendance);
                viewHolder.name = convertView.findViewById(R.id.subject_name);
                viewHolder.lastUpdated = convertView.findViewById(R.id.subject_last_updated);
                viewHolder.status = convertView.findViewById(R.id.subject_status);
                viewHolder.labLabel = convertView.findViewById(R.id.subject_lab_label);
                viewHolder.oldLab = convertView.findViewById(R.id.subject_old_lab);
                viewHolder.lab = convertView.findViewById(R.id.subject_lab);
                viewHolder.theoryLabel = convertView.findViewById(R.id.subject_theory_label);
                viewHolder.oldTheory = convertView.findViewById(R.id.subject_old_theory);
                viewHolder.theory = convertView.findViewById(R.id.subject_theory);
                viewHolder.oldAbsent = convertView.findViewById(R.id.subject_old_absent);
                viewHolder.absent = convertView.findViewById(R.id.subject_absent);
                viewHolder.bunkStats = convertView.findViewById(R.id.subject_bunk_stats);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final SubjectView subjectView = getItem(position);

            viewHolder.lastUpdated.setTextColor(getResources().getColor(R.color.black));
            viewHolder.labLabel.setVisibility(View.VISIBLE);
            viewHolder.oldLab.setVisibility(View.GONE);
            viewHolder.lab.setVisibility(View.VISIBLE);
            viewHolder.theoryLabel.setVisibility(View.VISIBLE);
            viewHolder.oldTheory.setVisibility(View.GONE);
            viewHolder.theory.setVisibility(View.VISIBLE);
            viewHolder.oldAbsent.setVisibility(View.GONE);

            viewHolder.avatar.setImageResource(subjectView.avatar);
            viewHolder.attendance.setText(subjectView.attendance);
            viewHolder.attendance.setBackgroundResource(subjectView.attendanceBadge);
            viewHolder.name.setText(subjectView.name);
            viewHolder.lastUpdated.setText(subjectView.lastUpdated);
            viewHolder.status.setImageResource(subjectView.status);
            viewHolder.oldLab.setText(subjectView.oldLab);
            viewHolder.lab.setText(subjectView.lab);
            viewHolder.oldTheory.setText(subjectView.oldTheory);
            viewHolder.theory.setText(subjectView.theory);
            viewHolder.oldAbsent.setText(subjectView.oldAbsent);
            viewHolder.absent.setText(subjectView.absent);
            viewHolder.bunkStats.setText(subjectView.bunkStats);

            if (subjectView.updated) {
                viewHolder.lastUpdated.setTextColor(getResources().getColor(R.color.blue));

                if (!StringUtils.equals(subjectView.lab, subjectView.oldLab)) {
                    viewHolder.oldLab.setVisibility(View.VISIBLE);
                    viewHolder.oldLab.setPaintFlags(viewHolder.oldLab.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (!StringUtils.equals(subjectView.theory, subjectView.oldTheory)) {
                    viewHolder.oldTheory.setVisibility(View.VISIBLE);
                    viewHolder.oldTheory.setPaintFlags(viewHolder.oldTheory.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (!StringUtils.equals(subjectView.absent, subjectView.oldAbsent)) {
                    viewHolder.oldAbsent.setVisibility(View.VISIBLE);
                    viewHolder.oldAbsent.setPaintFlags(viewHolder.oldAbsent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }

            if (StringUtils.equals(subjectView.lab, "0/0 classes")) {
                viewHolder.labLabel.setVisibility(View.GONE);
                viewHolder.oldLab.setVisibility(View.GONE);
                viewHolder.lab.setVisibility(View.GONE);
            }
            if (StringUtils.equals(subjectView.theory, "0/0 classes")) {
                viewHolder.theoryLabel.setVisibility(View.GONE);
                viewHolder.oldTheory.setVisibility(View.GONE);
                viewHolder.theory.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
