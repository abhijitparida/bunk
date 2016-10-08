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

package app.abhijit.iter.data.source;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.format.DateUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.abhijit.iter.R;
import app.abhijit.iter.data.model.Error;
import app.abhijit.iter.data.model.Student;
import app.abhijit.iter.data.model.Subject;
import app.abhijit.iter.data.source.remote.IterApi;

public class StudentDataSource {

    public static final String LOCAL_STORE_NAME = "bunk-v2";

    private static final String SELECTED_REGISTRATION_NUMBER_KEY = "selected";

    private static final String STUDENT_NAME_KEY = "name";
    private static final String STUDENT_REGISTRATION_NUMBER_KEY = "registrationnumber";
    private static final String STUDENT_ID_KEY = "id";
    private static final String STUDENT_TIMESTAMP_KEY = "timestamp";
    private static final String STUDENT_SUBJECTS_KEY = "subjects";

    private static final String STUDENT_SUBJECT_CODE_KEY = "code";
    private static final String STUDENT_SUBJECT_NAME_KEY = "name";
    private static final String STUDENT_SUBJECT_PRESENT_CLASSES_KEY = "presentclasses";
    private static final String STUDENT_SUBJECT_TOTAL_CLASSES_KEY = "totalclasses";
    private static final String STUDENT_SUBJECT_OLD_PRESENT_CLASSES_KEY = "oldpresentclasses";
    private static final String STUDENT_SUBJECT_OLD_TOTAL_CLASSES_KEY = "oldtotalclasses";
    private static final String STUDENT_SUBJECT_LAST_UPDATED_KEY = "lastupdated";

    private static final long CACHE_TIMEOUT = 600000; // 600000 milliseconds = 10 minutes

    private SharedPreferences mLocalStore;
    private String mSelectedRegistrationNumber;
    private String mErrorMessage;
    private FetchStudentAsyncTask mFetchStudentAsyncTask;
    private FetchStudentSubjectsAsyncTask mFetchStudentSubjectsAsyncTask;
    private JsonParser mJsonParser;
    HashMap<String, Integer> mSubjectAvatars;

    public StudentDataSource(SharedPreferences localStore) {
        mLocalStore = localStore;
        mJsonParser = new JsonParser();
        mSubjectAvatars = new HashMap<>();
        mSubjectAvatars.put("CHM1002", R.drawable.ic_subject_environmental_studies);
        mSubjectAvatars.put("CSE1002", R.drawable.ic_subject_maths);
        mSubjectAvatars.put("CSE1011", R.drawable.ic_subject_electrical);
        mSubjectAvatars.put("CSE2031", R.drawable.ic_subject_maths);
        mSubjectAvatars.put("CSE3151", R.drawable.ic_subject_database);
        mSubjectAvatars.put("CSE4042", R.drawable.ic_subject_network);
        mSubjectAvatars.put("CSE4043", R.drawable.ic_subject_security);
        mSubjectAvatars.put("CSE4044", R.drawable.ic_subject_security);
        mSubjectAvatars.put("CSE4051", R.drawable.ic_subject_database);
        mSubjectAvatars.put("CSE4052", R.drawable.ic_subject_database);
        mSubjectAvatars.put("CSE4053", R.drawable.ic_subject_database);
        mSubjectAvatars.put("CSE4054", R.drawable.ic_subject_database);
        mSubjectAvatars.put("CSE4102", R.drawable.ic_subject_html);
        mSubjectAvatars.put("CSE4141", R.drawable.ic_subject_android);
        mSubjectAvatars.put("CSE4151", R.drawable.ic_subject_server);
        mSubjectAvatars.put("CVL3071", R.drawable.ic_subject_traffic);
        mSubjectAvatars.put("CVL3241", R.drawable.ic_subject_water);
        mSubjectAvatars.put("CVL4031", R.drawable.ic_subject_earth);
        mSubjectAvatars.put("CVL4032", R.drawable.ic_subject_soil);
        mSubjectAvatars.put("CVL4041", R.drawable.ic_subject_water);
        mSubjectAvatars.put("CVL4042", R.drawable.ic_subject_water);
        mSubjectAvatars.put("EET1001", R.drawable.ic_subject_matlab);
        mSubjectAvatars.put("EET3041", R.drawable.ic_subject_electromagnetic_waves);
        mSubjectAvatars.put("EET3061", R.drawable.ic_subject_communication);
        mSubjectAvatars.put("EET3062", R.drawable.ic_subject_communication);
        mSubjectAvatars.put("EET4014", R.drawable.ic_subject_renewable_energy);
        mSubjectAvatars.put("EET4041", R.drawable.ic_subject_electromagnetic_waves);
        mSubjectAvatars.put("EET4061", R.drawable.ic_subject_wifi);
        mSubjectAvatars.put("EET4063", R.drawable.ic_subject_communication);
        mSubjectAvatars.put("EET4161", R.drawable.ic_subject_communication);
        mSubjectAvatars.put("HSS1001", R.drawable.ic_subject_effective_speech);
        mSubjectAvatars.put("HSS1021", R.drawable.ic_subject_economics);
        mSubjectAvatars.put("HSS2021", R.drawable.ic_subject_economics);
        mSubjectAvatars.put("MEL3211", R.drawable.ic_subject_water);
        mSubjectAvatars.put("MTH2002", R.drawable.ic_subject_probability_statistics);
        mSubjectAvatars.put("MTH4002", R.drawable.ic_subject_matlab);
        mSubjectAvatars.put("CHM", R.drawable.ic_subject_chemistry);
        mSubjectAvatars.put("CSE", R.drawable.ic_subject_computer);
        mSubjectAvatars.put("CVL", R.drawable.ic_subject_civil);
        mSubjectAvatars.put("EET", R.drawable.ic_subject_electrical);
        mSubjectAvatars.put("HSS", R.drawable.ic_subject_humanities);
        mSubjectAvatars.put("MEL", R.drawable.ic_subject_mechanical);
        mSubjectAvatars.put("MTH", R.drawable.ic_subject_maths);
        mSubjectAvatars.put("PHY", R.drawable.ic_subject_physics);
        mSubjectAvatars.put("generic", R.drawable.ic_subject_generic);
    }

    public void fetch() {
        String selectedRegistrationNumber = mLocalStore.getString(SELECTED_REGISTRATION_NUMBER_KEY, null);
        if (!StringUtils.equals(selectedRegistrationNumber, mSelectedRegistrationNumber)) {
            if (mFetchStudentAsyncTask != null) {
                mFetchStudentAsyncTask.cancel(true);
                mFetchStudentAsyncTask = null;
            }
            if (mFetchStudentSubjectsAsyncTask != null) {
                mFetchStudentSubjectsAsyncTask.cancel(true);
                mFetchStudentSubjectsAsyncTask = null;
            }
            mSelectedRegistrationNumber = selectedRegistrationNumber;
        }

        if (mSelectedRegistrationNumber != null && mErrorMessage == null && mFetchStudentAsyncTask == null && mFetchStudentSubjectsAsyncTask == null) {
            if (!mLocalStore.contains(mSelectedRegistrationNumber)) {
                JsonObject student = new JsonObject();
                student.addProperty(STUDENT_REGISTRATION_NUMBER_KEY, mSelectedRegistrationNumber);
                mLocalStore.edit().putString(mSelectedRegistrationNumber, student.toString()).apply();
                mFetchStudentAsyncTask = new FetchStudentAsyncTask();
                mFetchStudentAsyncTask.execute();
            } else {
                JsonObject student = mJsonParser.parse(mLocalStore.getString(mSelectedRegistrationNumber, null)).getAsJsonObject();
                if (!student.has(STUDENT_NAME_KEY)) {
                    mFetchStudentAsyncTask = new FetchStudentAsyncTask();
                    mFetchStudentAsyncTask.execute();
                } else {
                    boolean isSubjectDataOld = (new Date().getTime() - student.get(STUDENT_TIMESTAMP_KEY).getAsLong()) > CACHE_TIMEOUT;
                    if (isSubjectDataOld || !student.has(STUDENT_SUBJECTS_KEY)) {
                        String studentId = student.get(STUDENT_ID_KEY).getAsString();
                        mFetchStudentSubjectsAsyncTask = new FetchStudentSubjectsAsyncTask();
                        mFetchStudentSubjectsAsyncTask.execute(studentId);
                    }
                }
            }
        }

        if (mErrorMessage != null) {
            EventBus.getDefault().post(new Error(mErrorMessage));
            mErrorMessage = null;
        }

        ArrayList<Student> students = new ArrayList<>();

        for (Map.Entry<String, ?> entry : mLocalStore.getAll().entrySet()) {
            if (entry.getKey().equals(SELECTED_REGISTRATION_NUMBER_KEY)) continue;
            JsonObject student = mJsonParser.parse(mLocalStore.getString(entry.getKey(), null)).getAsJsonObject();
            students.add(generateStudent(student));
        }

        EventBus.getDefault().post(ImmutableList.copyOf(students));
    }

    public void refresh() {
        if (mSelectedRegistrationNumber != null && mFetchStudentAsyncTask == null && mFetchStudentSubjectsAsyncTask == null) {
            JsonObject student = mJsonParser.parse(mLocalStore.getString(mSelectedRegistrationNumber, null)).getAsJsonObject();
            student.addProperty(STUDENT_TIMESTAMP_KEY, 0);
            mLocalStore.edit().putString(mSelectedRegistrationNumber, student.toString()).apply();
        }
        fetch();
    }

    public void select(String registrationNumber) {
        mLocalStore.edit().putString(SELECTED_REGISTRATION_NUMBER_KEY, registrationNumber).apply();
        fetch();
    }

    public void delete(String registrationNumber) {
        if (mLocalStore.contains(registrationNumber)) {
            mLocalStore.edit().remove(registrationNumber).apply();
            String selectedRegistrationNumber = mLocalStore.getString(SELECTED_REGISTRATION_NUMBER_KEY, null);
            if (StringUtils.equals(selectedRegistrationNumber, registrationNumber)) {
                select(null);
            }
            fetch();
        }
    }

    private void processStudent(String[] response) {
        mFetchStudentAsyncTask = null;

        if (response[0] == null || response[1] == null) {
            mErrorMessage = "Could not load registration number " + mSelectedRegistrationNumber;
            delete(mSelectedRegistrationNumber);
            fetch();
            return;
        }

        String studentId = response[0];

        if (studentId.equals("0")) {
            mErrorMessage = "Registration number " + mSelectedRegistrationNumber + " is not valid";
            delete(mSelectedRegistrationNumber);
            fetch();
            return;
        }

        JsonObject student = new JsonObject();

        try {
            JsonObject studentDetails = mJsonParser.parse(response[1]).getAsJsonArray().get(0).getAsJsonObject();
            if (!studentDetails.has("name")) {
                throw new Exception();
            }
            student.addProperty(STUDENT_ID_KEY, studentId);
            student.addProperty(STUDENT_NAME_KEY, studentDetails.get("name").getAsString());
            student.addProperty(STUDENT_REGISTRATION_NUMBER_KEY, mSelectedRegistrationNumber);
            student.addProperty(STUDENT_TIMESTAMP_KEY, new Date().getTime());
        } catch (Exception e) {
            mErrorMessage = "Could not load registration number " + mSelectedRegistrationNumber;
            delete(mSelectedRegistrationNumber);
            fetch();
            return;
        }

        mLocalStore.edit().putString(mSelectedRegistrationNumber, student.toString()).apply();

        fetch();
    }

    private void processStudentSubjects(String response) {
        mFetchStudentSubjectsAsyncTask = null;

        JsonObject student = mJsonParser.parse(mLocalStore.getString(mSelectedRegistrationNumber, null)).getAsJsonObject();
        JsonObject subjects = new JsonObject();

        try {
            JsonArray studentSubjects = mJsonParser.parse(response).getAsJsonArray();
            for (int i = 0; i < studentSubjects.size(); i++) {
                if (!studentSubjects.get(i).getAsJsonObject().has("subjectcode") ||
                        !studentSubjects.get(i).getAsJsonObject().has("subject") ||
                        !studentSubjects.get(i).getAsJsonObject().has("totalpresentclass") ||
                        !studentSubjects.get(i).getAsJsonObject().has("totalclasses") ||
                        studentSubjects.get(i).getAsJsonObject().get("totalclasses").getAsInt() == 0) {
                    continue;
                }
                String code = studentSubjects.get(i).getAsJsonObject().get("subjectcode").getAsString();
                String name = studentSubjects.get(i).getAsJsonObject().get("subject").getAsString();
                int presentClasses = studentSubjects.get(i).getAsJsonObject().get("totalpresentclass").getAsInt();
                int totalClasses = studentSubjects.get(i).getAsJsonObject().get("totalclasses").getAsInt();
                long lastUpdated = new Date().getTime();
                int oldPresentClasses = presentClasses;
                int oldTotalClasses = totalClasses;
                if (student.has(STUDENT_SUBJECTS_KEY) && student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().has(code)) {
                    oldPresentClasses = student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().get(code).getAsJsonObject().get(STUDENT_SUBJECT_PRESENT_CLASSES_KEY).getAsInt();
                    oldTotalClasses = student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().get(code).getAsJsonObject().get(STUDENT_SUBJECT_TOTAL_CLASSES_KEY).getAsInt();
                    if (presentClasses == oldPresentClasses && totalClasses == oldTotalClasses) {
                        lastUpdated = student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().get(code).getAsJsonObject().get(STUDENT_SUBJECT_LAST_UPDATED_KEY).getAsLong();
                    }
                }
                JsonObject subject = new JsonObject();
                subject.addProperty(STUDENT_SUBJECT_CODE_KEY, code);
                subject.addProperty(STUDENT_SUBJECT_NAME_KEY, name);
                subject.addProperty(STUDENT_SUBJECT_PRESENT_CLASSES_KEY, presentClasses);
                subject.addProperty(STUDENT_SUBJECT_TOTAL_CLASSES_KEY, totalClasses);
                subject.addProperty(STUDENT_SUBJECT_OLD_PRESENT_CLASSES_KEY, oldPresentClasses);
                subject.addProperty(STUDENT_SUBJECT_OLD_TOTAL_CLASSES_KEY, oldTotalClasses);
                subject.addProperty(STUDENT_SUBJECT_LAST_UPDATED_KEY, lastUpdated);
                subjects.add(code, subject);
            }
        } catch (Exception e) {
            if (student.has(STUDENT_SUBJECTS_KEY)) {
                subjects = student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject();
            } else {
                subjects = new JsonObject();
            }
        }

        student.add(STUDENT_SUBJECTS_KEY, subjects);
        student.addProperty(STUDENT_TIMESTAMP_KEY, new Date().getTime());
        mLocalStore.edit().putString(mSelectedRegistrationNumber, student.toString()).apply();

        fetch();
    }

    private Student generateStudent(JsonObject student) {
        String registrationNumber = student.get(STUDENT_REGISTRATION_NUMBER_KEY).getAsString();
        String name = null;
        if (student.has(STUDENT_NAME_KEY)) {
            name = WordUtils.capitalizeFully(student.get(STUDENT_NAME_KEY).getAsString());
        }
        boolean selected = StringUtils.equals(registrationNumber, mSelectedRegistrationNumber);

        ArrayList<Subject> subjects = new ArrayList<>();

        if (StringUtils.equals(mSelectedRegistrationNumber, registrationNumber) && mFetchStudentSubjectsAsyncTask == null && student.has(STUDENT_SUBJECTS_KEY)) {
            for (Map.Entry<String, ?> entry : student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().entrySet()) {
                JsonObject subject = student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().get(entry.getKey()).getAsJsonObject();
                subjects.add(generateStudentSubject(subject));
                removeOldSubjectData(registrationNumber, student, subject);
            }
        } else {
            subjects = null;
        }

        return new Student(
                name,
                registrationNumber,
                selected,
                subjects == null ? null : ImmutableList.copyOf(subjects)
        );
    }

    private Subject generateStudentSubject(JsonObject subject) {
        String name = subject.get(STUDENT_SUBJECT_NAME_KEY).getAsString();
        String code = subject.get(STUDENT_SUBJECT_CODE_KEY).getAsString();
        int presentClasses = subject.get(STUDENT_SUBJECT_PRESENT_CLASSES_KEY).getAsInt();
        int oldPresentClasses = subject.get(STUDENT_SUBJECT_OLD_PRESENT_CLASSES_KEY).getAsInt();
        int totalClasses = subject.get(STUDENT_SUBJECT_TOTAL_CLASSES_KEY).getAsInt();
        int oldTotalClasses = subject.get(STUDENT_SUBJECT_OLD_TOTAL_CLASSES_KEY).getAsInt();
        long lastUpdated = subject.get(STUDENT_SUBJECT_LAST_UPDATED_KEY).getAsLong();

        float attendance = (presentClasses / (float) totalClasses) * 100;
        float oldAttendance = (oldPresentClasses / (float) oldTotalClasses) * 100;

        int absentClasses = totalClasses - presentClasses;
        int oldAbsentClasses = oldTotalClasses - oldPresentClasses;

        int status;
        if (attendance == oldAttendance) {
            if (attendance > 85) {
                status = R.drawable.ic_status_ok;
            } else if (attendance > 75) {
                status = R.drawable.ic_status_warning;
            } else {
                status = R.drawable.ic_status_critical;
            }
        } else {
            if (attendance > oldAttendance) {
                status = R.drawable.ic_status_up;
            } else {
                status = R.drawable.ic_status_down;
            }
        }

        int subjectAvatar = mSubjectAvatars.get("generic");
        if (mSubjectAvatars.containsKey(code)) {
            subjectAvatar = mSubjectAvatars.get(code);
        } else if (code.length() >= 3 && mSubjectAvatars.containsKey(code.substring(0, 3))) {
            subjectAvatar = mSubjectAvatars.get(code.substring(0, 3));
        }

        return new Subject(
                subjectAvatar,
                name,
                attendance == oldAttendance ? null : String.format(Locale.US, "%.2f", oldAttendance) + "%",
                String.format(Locale.US, "%.2f", attendance) + "%",
                status,
                DateUtils.getRelativeTimeSpanString(lastUpdated, new Date().getTime(), 0).toString(),
                presentClasses == oldPresentClasses && totalClasses == oldTotalClasses ? null : oldPresentClasses + "/" + oldTotalClasses + (oldTotalClasses == 1 ? " class" : " classes"),
                presentClasses + "/" + totalClasses + (totalClasses == 1 ? " class" : " classes"),
                absentClasses == oldAbsentClasses ? null : oldAbsentClasses + (oldAbsentClasses == 1 ? " class" : " classes"),
                absentClasses + (absentClasses == 1 ? " class" : " classes"),
                generateBunkStats((int) attendance, totalClasses, presentClasses, absentClasses)
        );
    }

    private void removeOldSubjectData(String registrationNumber, JsonObject student, JsonObject subject) {
        String code = subject.get(STUDENT_SUBJECT_CODE_KEY).getAsString();
        int presentClasses = subject.get(STUDENT_SUBJECT_PRESENT_CLASSES_KEY).getAsInt();
        int totalClasses = subject.get(STUDENT_SUBJECT_TOTAL_CLASSES_KEY).getAsInt();
        subject.addProperty(STUDENT_SUBJECT_OLD_PRESENT_CLASSES_KEY, presentClasses);
        subject.addProperty(STUDENT_SUBJECT_OLD_TOTAL_CLASSES_KEY, totalClasses);
        student.get(STUDENT_SUBJECTS_KEY).getAsJsonObject().add(code, subject);
        mLocalStore.edit().putString(registrationNumber, student.toString()).apply();
    }

    private String generateBunkStats(final int attendance, final int totalClasses, final int presentClasses, final int absentClasses) {
        String bunkStats = "";

        if (attendance <= 75) {
            bunkStats += "DO NOT BUNK ANY MORE CLASSES\n";
        } else {
            for (int a = 75; a < attendance; a += 5) {
                int daysBunk = (int) ((100 * presentClasses / (float) a) - (float) totalClasses);
                if (daysBunk > 0) {
                    bunkStats += "Bunk " + daysBunk + (absentClasses == 0 ? "" : " more") + (daysBunk == 1 ? " class" : " classes") + " for " + a + "% attendance\n";
                }
            }
        }

        int nextAttendance = (attendance + 4) / 5 * 5;
        if (nextAttendance == attendance) nextAttendance = attendance + 5;
        if (nextAttendance < 75) nextAttendance = 75;
        for (int a = nextAttendance; a <= 95; a += 5) {
            int daysNeed = (int) ((a * totalClasses - 100 * presentClasses) / (float) (100 - a));
            if (daysNeed > 0 && (daysNeed + totalClasses <= 50)) {
                bunkStats += "Need " + daysNeed + " more" + (daysNeed == 1 ? " class" : " classes") + " for " + a + "% attendance\n";
            }
        }

        return bunkStats.equals("") ? null : bunkStats.substring(0, bunkStats.length() - 1);
    }

    private class FetchStudentAsyncTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                String studentId = IterApi.fetchStudentId(mSelectedRegistrationNumber);
                String studentDetails = studentId.equals("0") ? "" : IterApi.fetchStudentDetails(studentId);
                return new String[]{studentId, studentDetails};
            } catch (Exception e) {
                return new String[]{null, null};
            }
        }

        @Override
        protected void onPostExecute(String[] response) {
            super.onPostExecute(response);
            processStudent(response);
        }
    }

    private class FetchStudentSubjectsAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String studentId = params[0];
            try {
                return IterApi.fetchStudentSubjects(studentId);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processStudentSubjects(response);
        }
    }
}
