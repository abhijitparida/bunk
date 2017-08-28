/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Abhijit Parida <abhijitparida.me@gmail.com>
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Random;

import app.abhijit.iter.data.Cache;
import app.abhijit.iter.models.Student;
import app.abhijit.iter.models.Subject;

public class AttendanceActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private Cache mCache;

    private Student mNewStudent;
    private Student mOldStudent;

    private boolean mPrefExtendedStats;
    private int mPrefMinimumAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        mContext = this;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mCache = new Cache(mContext);

        try {
            mNewStudent = new Gson().fromJson(getIntent().getStringExtra("student"), Student.class);
        } catch (Exception ignored) { }
        mOldStudent = mCache.getStudent(mSharedPreferences.getString("pref_student", null));
        if (mNewStudent == null && mOldStudent == null) {
            startActivity(new Intent(AttendanceActivity.this, LoginActivity.class));
            finish();
        }
        if (mNewStudent == null) mNewStudent = mOldStudent;
        if (mOldStudent == null) mOldStudent = mNewStudent;

        mPrefExtendedStats = mSharedPreferences.getBoolean("pref_extended_stats", false);
        mPrefMinimumAttendance = mSharedPreferences.getInt("pref_minimum_attendance", 75);

        processAndDisplayAttendance();

        setupToolbar();
        setupDrawer();
        setupFab();
        setupListView();

        if (!BuildConfig.DEBUG) {
            displayBannerAd();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean prefExtendedStats = mSharedPreferences.getBoolean("pref_extended_stats", false);
        int prefMinimumAttendance = mSharedPreferences.getInt("pref_minimum_attendance", 75);

        if (mPrefExtendedStats != prefExtendedStats
                || mPrefMinimumAttendance != prefMinimumAttendance) {
            mPrefExtendedStats = prefExtendedStats;
            mPrefMinimumAttendance = prefMinimumAttendance;

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
            // ¯\_(ツ)_/¯
        } else if (id == R.id.action_logout) {
            mCache.setStudent(mSharedPreferences.getString("pref_student", null), null);
            Toast.makeText(mContext, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AttendanceActivity.this, LoginActivity.class));
            finish();
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(AttendanceActivity.this, SettingsActivity.class));
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) {
            startActivity(new Intent(AttendanceActivity.this, AboutActivity.class));
        } else if (id == R.id.nav_feedback) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_feedback))));
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
        ((TextView) navigationViewHeader.findViewById(R.id.name)).setText(mNewStudent.name);
        ((TextView) navigationViewHeader.findViewById(R.id.username)).setText(mNewStudent.username);
        String prompts[] = {"open source?", "coding?", "programming?", "code+coffee?"};
        TextView opensource = drawer.findViewById(R.id.opensource);
        opensource.setText(prompts[new Random().nextInt(prompts.length)]);
        TextView github = drawer.findViewById(R.id.github);
        github.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_github))));
            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSharedPreferences.edit().putString("pref_student", null).apply();
                startActivity(new Intent(AttendanceActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void setupListView() {
        ListView subjectsList = findViewById(R.id.subjects);
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        subjectsList.addFooterView(layoutInflater.inflate(R.layout.listview_footer, null, false));
    }

    private void displayBannerAd() {
        MobileAds.initialize(mContext, getResources().getString(R.string.banner_ad_unit_id));
        AdView adView = findViewById(R.id.ad);
        adView.setVisibility(View.VISIBLE);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void processAndDisplayAttendance() {
        Gson gson = new Gson();
        Student student = gson.fromJson(gson.toJson(mNewStudent), Student.class);
        Boolean updated = false;
        for (Subject subject : student.subjects.values()) {
            String subjectCode = subject.code;
            if (!mOldStudent.subjects.containsKey(subjectCode)) continue;
            Subject oldSubject = mOldStudent.subjects.get(subjectCode);
            if (subject.theoryPresent != oldSubject.theoryPresent
                    || subject.theoryTotal != oldSubject.theoryTotal
                    || subject.labPresent != oldSubject.labPresent
                    || subject.labTotal != oldSubject.labTotal) {
                updated = true;
            } else {
                subject.lastUpdated = oldSubject.lastUpdated;
            }
        }
        mCache.setStudent(student.username, student);

        if (updated) {
            Toast.makeText(mContext, "Attendance updated", Toast.LENGTH_SHORT).show();
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }
        findViewById(R.id.no_attendance).setVisibility(student.subjects.isEmpty() ? View.VISIBLE : View.GONE);
        ListView subjectsList = findViewById(R.id.subjects);
        SubjectsAdapter subjectsAdapter = new SubjectsAdapter(new ArrayList<>(student.subjects.values()));
        subjectsList.setAdapter(subjectsAdapter);
    }

    private class SubjectsAdapter extends ArrayAdapter<Subject> {

        private final LayoutInflater mLayoutInflater;

        SubjectsAdapter(ArrayList<Subject> subjects) {
            super(mContext, R.layout.item_subject, subjects);

            mLayoutInflater = LayoutInflater.from(mContext);
        }

        private class ViewHolder {

        }

        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item_subject, parent, false);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Subject subject = getItem(position);

            return convertView;
        }
    }
}
