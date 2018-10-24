package app.abhijit.iter.models;

import java.util.HashMap;

/**
 * This class represents a student.
 */
public class Student {

    public String username;
    public String password;
    public String name;
    public HashMap<String, Subject> subjects = new HashMap<>();
}
