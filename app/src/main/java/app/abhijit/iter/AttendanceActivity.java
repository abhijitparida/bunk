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
import android.os.Handler;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
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
    private ArrayList<SubjectView> mSubjectViews;
    private SubjectAdapter mSubjectAdapter;
    private int mRefresh;

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

        mPrefExtendedStats = mSharedPreferences.getBoolean("pref_extended_stats", true);
        mPrefMinimumAttendance = Integer.parseInt(mSharedPreferences.getString("pref_minimum_attendance", "75"));

        setupToolbar();
        setupDrawer();
        setupFab();
        setupListView();

        processAndDisplayAttendance();

        if (!BuildConfig.DEBUG) {
            displayBannerAd();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean prefExtendedStats = mSharedPreferences.getBoolean("pref_extended_stats", true);
        int prefMinimumAttendance = Integer.parseInt(mSharedPreferences.getString("pref_minimum_attendance", "75"));

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
                public void onAnimationStart(Animation animation) {
                    if (++mRefresh == 5) {
                        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(2000);
                        findViewById(R.id.chicken).setVisibility(View.VISIBLE); // ¯\_(ツ)_/¯
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.chicken).setVisibility(View.GONE);
                            }
                        }, 2000);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    item.getActionView().clearAnimation();
                    item.setActionView(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
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
        mSubjectViews = new ArrayList<>();
        mSubjectAdapter = new SubjectAdapter(mSubjectViews);
        subjectsList.setAdapter(mSubjectAdapter);
    }

    private void displayBannerAd() {
        AdView adView = findViewById(R.id.ad);
        adView.setVisibility(View.VISIBLE);
        FloatingActionButton fab = findViewById(R.id.fab);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fab.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        fab.setLayoutParams(layoutParams);

        MobileAds.initialize(mContext, getResources().getString(R.string.banner_ad_unit_id));
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void processAndDisplayAttendance() {
        ArrayList<SubjectView> subjectViews = new ArrayList<>();
        Boolean updated = false;
        for (Subject subject : mNewStudent.subjects.values()) {
            SubjectView subjectView = new SubjectView();
            subjectView.avatar = subjectAvatar(subject.code);
            subjectView.name = subject.name;
            subjectView.attendance = String.format(Locale.US, "%.2f%%", subject.attendance());
            subjectView.theory = String.format(Locale.US, "%d/%d classes", subject.theoryPresent, subject.theoryTotal);
            subjectView.lab = String.format(Locale.US, "%d/%d classes", subject.labPresent, subject.labTotal);
            subjectView.absent = String.format(Locale.US, "%d classes", subject.absent());
            if (mOldStudent.subjects.containsKey(subject.code)) {
                Subject oldSubject = mOldStudent.subjects.get(subject.code);
                if (subject.theoryPresent != oldSubject.theoryPresent
                        || subject.theoryTotal != oldSubject.theoryTotal
                        || subject.labPresent != oldSubject.labPresent
                        || subject.labTotal != oldSubject.labTotal) {
                    updated = true;
                    subjectView.oldAttendance = String.format(Locale.US, "%.2f%%", oldSubject.attendance());
                    subjectView.oldTheory = String.format(Locale.US, "%d/%d classes", oldSubject.theoryPresent, oldSubject.theoryTotal);
                    subjectView.oldLab = String.format(Locale.US, "%d/%d classes", oldSubject.labPresent, oldSubject.labTotal);
                    subjectView.oldAbsent = String.format(Locale.US, "%d classes", oldSubject.absent());
                    if (subject.attendance() >= oldSubject.attendance()) {
                        subjectView.status = R.drawable.ic_status_up;
                    } else {
                        subjectView.status = R.drawable.ic_status_down;
                    }
                } else {
                    subject.lastUpdated = oldSubject.lastUpdated;
                    if (subject.attendance() > 85.0) {
                        subjectView.status = R.drawable.ic_status_ok;
                    } else if (subject.attendance() > 75.0) {
                        subjectView.status = R.drawable.ic_status_warning;
                    } else {
                        subjectView.status = R.drawable.ic_status_critical;
                    }
                }
            }
            subjectView.bunkStats = subject.bunkStats(mPrefMinimumAttendance, mPrefExtendedStats);
            subjectView.lastUpdated = DateUtils.getRelativeTimeSpanString(subject.lastUpdated, new Date().getTime(), 0).toString();
            subjectViews.add(subjectView);
        }
        mCache.setStudent(mNewStudent.username, mNewStudent);

        if (updated) {
            Toast.makeText(mContext, "Attendance updated", Toast.LENGTH_SHORT).show();
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }

        findViewById(R.id.no_attendance).setVisibility(subjectViews.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.subjects).setVisibility(subjectViews.isEmpty() ? View.GONE : View.VISIBLE);

        mSubjectViews.clear();
        mSubjectViews.addAll(subjectViews);
        mSubjectAdapter.notifyDataSetChanged();
    }

    private int subjectAvatar(String subjectCode) {
        int avatar;
        String code = subjectCode.substring(0, Math.min(subjectCode.length(), 3));
        switch (code) {
            case "CHM": avatar = R.drawable.ic_subject_chemistry; break;
            case "CSE": avatar = R.drawable.ic_subject_computer; break;
            case "CVL": avatar = R.drawable.ic_subject_civil; break;
            case "EET": avatar = R.drawable.ic_subject_electrical; break;
            case "HSS": avatar = R.drawable.ic_subject_humanities; break;
            case "MEL": avatar = R.drawable.ic_subject_mechanical; break;
            case "MTH": avatar = R.drawable.ic_subject_maths; break;
            case "PHY": avatar = R.drawable.ic_subject_physics; break;
            default: avatar = R.drawable.ic_subject_generic;
        }

        return avatar;

    }

    private class SubjectView {

        private int avatar;
        private String name;
        private String oldAttendance;
        private String attendance;
        private int status;
        private String lastUpdated;
        private String oldTheory;
        private String theory;
        private String oldLab;
        private String lab;
        private String oldAbsent;
        private String absent;
        private String bunkStats;
    }

    private class SubjectAdapter extends ArrayAdapter<SubjectView> {

        private final LayoutInflater mLayoutInflater;

        SubjectAdapter(ArrayList<SubjectView> subjectViews) {
            super(mContext, R.layout.item_subject, subjectViews);

            mLayoutInflater = LayoutInflater.from(mContext);
        }

        private class ViewHolder {

            private ImageView avatar;
            private TextView name;
            private TextView oldAttendance;
            private TextView attendance;
            private ImageView status;
            private TextView lastUpdated;
            private TextView oldTheory;
            private TextView theory;
            private TextView oldLab;
            private TextView lab;
            private TextView oldAbsent;
            private TextView absent;
            private TextView bunkStats;
        }

        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item_subject, parent, false);
                viewHolder.avatar = convertView.findViewById(R.id.subject_avatar);
                viewHolder.name = convertView.findViewById(R.id.subject_name);
                viewHolder.oldAttendance = convertView.findViewById(R.id.subject_old_attendance);
                viewHolder.attendance = convertView.findViewById(R.id.subject_attendance);
                viewHolder.status = convertView.findViewById(R.id.subject_status);
                viewHolder.lastUpdated = convertView.findViewById(R.id.subject_last_updated);
                viewHolder.oldTheory = convertView.findViewById(R.id.subject_old_theory);
                viewHolder.theory = convertView.findViewById(R.id.subject_theory);
                viewHolder.oldLab = convertView.findViewById(R.id.subject_old_lab);
                viewHolder.lab = convertView.findViewById(R.id.subject_lab);
                viewHolder.oldAbsent = convertView.findViewById(R.id.subject_old_absent);
                viewHolder.absent = convertView.findViewById(R.id.subject_absent);
                viewHolder.bunkStats = convertView.findViewById(R.id.subject_bunk_stats);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final SubjectView subjectView = getItem(position);
            viewHolder.avatar.setImageResource(subjectView.avatar);
            viewHolder.name.setText(subjectView.name);
            if (subjectView.oldAttendance != null) {
                viewHolder.oldAttendance.setText(subjectView.oldAttendance);
                viewHolder.oldAttendance.setVisibility(View.VISIBLE);
                viewHolder.oldAttendance.setPaintFlags(viewHolder.oldAttendance.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.attendance.setText(subjectView.attendance);
            viewHolder.status.setImageResource(subjectView.status);
            viewHolder.lastUpdated.setText(subjectView.lastUpdated);
            if (subjectView.oldTheory != null) {
                viewHolder.oldTheory.setText(subjectView.oldTheory);
                viewHolder.oldTheory.setVisibility(View.VISIBLE);
                viewHolder.oldTheory.setPaintFlags(viewHolder.oldTheory.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.theory.setText(subjectView.theory);
            if (subjectView.oldLab != null) {
                viewHolder.oldLab.setText(subjectView.oldLab);
                viewHolder.oldLab.setVisibility(View.VISIBLE);
                viewHolder.oldLab.setPaintFlags(viewHolder.oldLab.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.lab.setText(subjectView.lab);
            if (subjectView.oldAbsent != null) {
                viewHolder.oldAbsent.setText(subjectView.oldAbsent);
                viewHolder.oldAbsent.setVisibility(View.VISIBLE);
                viewHolder.oldAbsent.setPaintFlags(viewHolder.oldAbsent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            viewHolder.absent.setText(subjectView.absent);
            viewHolder.bunkStats.setText(subjectView.bunkStats);

            return convertView;
        }
    }
}
