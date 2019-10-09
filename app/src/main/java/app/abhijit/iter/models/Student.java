package app.abhijit.iter.models;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a student.
 */
public class Student {

    private String username;
    private String password;
    private String name;
    private Map<String, Subject> subjects = new HashMap<>();

    public Student() {
    }

    public Student(String username, String name, String password) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Map<String, Subject> subjects) {
        this.subjects = subjects;
    }
}
