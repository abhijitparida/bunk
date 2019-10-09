package app.abhijit.iter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import app.abhijit.iter.models.Student;

/**
 * This class handles local storage and retrieval of Student objects.
 */
public class Cache {

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public Cache(Context context) {
        this.sharedPreferences = context.getSharedPreferences("students", Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    @Nullable
    public Student getStudent(@Nullable String username) {
        try {
            return gson.fromJson(this.sharedPreferences.getString(username, null), Student.class);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    public ArrayList<Student> getStudents() {
        ArrayList<Student> students = new ArrayList<>();
        for (Map.Entry<String, ?> entry : this.sharedPreferences.getAll().entrySet()) {
            Student student = getStudent(entry.getKey());
            if (student != null) {
                students.add(student);
            }
        }

        return students;
    }

    public void setStudent(@Nullable String username, @Nullable Student student) {
        if (student == null) {
            deleteStudent(username);
        } else {
            this.sharedPreferences.edit().putString(username, gson.toJson(student)).apply();
        }
    }

    public void deleteStudent(@Nullable String username) {
        this.sharedPreferences.edit().remove(username).apply();
    }
}
