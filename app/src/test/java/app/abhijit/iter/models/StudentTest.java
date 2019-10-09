package app.abhijit.iter.models;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class StudentTest {

    private static final String STUDENT_USERNAME = "0123456789";
    private static final String STUDENT_NAME = "Pedro";
    private static final String STUDENT_PASSWORD = "password";

    @Test
    public void getUsername_SimpleValue_getsCorrectly() {
        Student student = new Student(STUDENT_USERNAME, null, null);

        assertEquals(STUDENT_USERNAME, student.getUsername());
    }

    @Test
    public void setUsername_SimpleValue_setsCorrectly() {
        Student student = new Student(STUDENT_USERNAME, STUDENT_NAME, STUDENT_PASSWORD);
        String newUsername = "newUsername";

        student.setUsername(newUsername);

        assertEquals(newUsername, student.getUsername());
    }

    @Test
    public void getName_SimpleValue_getsCorrectly() {
        Student student = new Student(null, STUDENT_NAME, null);

        assertEquals(STUDENT_NAME, student.getName());
    }

    @Test
    public void setName_SimpleValue_setsCorrectly() {
        Student student = new Student(STUDENT_USERNAME, STUDENT_NAME, STUDENT_PASSWORD);
        String newName = "newName";

        student.setName(newName);

        assertEquals(newName, student.getName());
    }

    @Test
    public void getPassword_SimpleValue_getsCorrectly() {
        Student student = new Student(null, null, STUDENT_PASSWORD);

        assertEquals(STUDENT_PASSWORD, student.getPassword());
    }

    @Test
    public void setPassword_SimpleValue_setsCorrectly() {
        Student student = new Student(STUDENT_USERNAME, STUDENT_NAME, STUDENT_PASSWORD);
        String newPassword = "newPassword";

        student.setPassword(newPassword);

        assertEquals(newPassword, student.getPassword());
    }

    @Test
    public void getSubjects_DefaultState_getsCorrectly() {
        Student student = new Student();

        assertEquals(0, student.getSubjects().size());
    }

    @Test
    public void setSubjects_SimpleValue_setsCorrectly() {
        Student student = new Student();
        Map<String, Subject> subjects = new HashMap<>();
        subjects.put(STUDENT_USERNAME, new Subject());

        student.setSubjects(subjects);

        assertEquals(subjects, student.getSubjects());
    }
}
