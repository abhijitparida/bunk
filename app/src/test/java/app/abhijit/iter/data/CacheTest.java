package app.abhijit.iter.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.Context;
import android.content.SharedPreferences;
import app.abhijit.iter.models.Student;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class CacheTest {

    private static final String USERNAME_1 = "1234567890";
    private static final String USERNAME_2 = "0987654321";

    private Cache cache;
    private Gson gson = new Gson();

    @Mock
    private Context context;

    @Mock
    private SharedPreferences sharedPreferences;

    @Mock
    private SharedPreferences.Editor editor;

    @Before
    public void setUp() {
        initMocks(this);

        when(context.getSharedPreferences("students", Context.MODE_PRIVATE))
                .thenReturn(sharedPreferences);

        cache = new Cache(context);
    }

    @Test
    public void getStudent_ValidUsername_GetsStudentCorrectly() {
        mockStudent(USERNAME_1);

        Student student = cache.getStudent(USERNAME_1);

        verify(sharedPreferences, times(1))
                .getString(USERNAME_1, null);
        assertEquals(USERNAME_1, student.username);
    }

    @Test
    public void getStudent_InvalidJsonFormat_GetsNullReturn() {
        when(sharedPreferences.getString(USERNAME_1, null))
                .thenReturn("invalidJSON");

        assertNull(cache.getStudent(USERNAME_1));
    }

    @Test
    public void getStudents_ValidUsernames_GetsStudentsCorrectly() {
        mockStudents(USERNAME_1, USERNAME_2);

        List<Student> students = cache.getStudents();

        assertEquals(2, students.size());
        assertEquals(USERNAME_1, students.get(0).username);
        assertEquals(USERNAME_2, students.get(1).username);
    }

    @Test
    public void setStudent_NullStudent_RemovesUsernameCorrectly() {
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.remove(USERNAME_1)).thenReturn(editor);

        cache.setStudent(USERNAME_1, null);

        verify(editor, times(1)).remove(USERNAME_1);
        verify(editor, times(1)).apply();
    }

    @Test
    public void setStudent_ValidStudent_PutsUsernameCorrectly() {
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);

        cache.setStudent(USERNAME_1, getStudent(USERNAME_1));

        verify(editor, times(1))
                .putString(USERNAME_1, getStudentJson(USERNAME_1));
        verify(editor, times(1)).apply();
    }

    @Test
    public void deleteStudent_NullStudent_RemovesUsernameCorrectly() {
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.remove(USERNAME_1)).thenReturn(editor);

        cache.deleteStudent(USERNAME_1);

        verify(editor, times(1)).remove(USERNAME_1);
        verify(editor, times(1)).apply();
    }

    private void mockStudents(String... usernames) {
        Map<String, String> studentsMap = new HashMap<>();

        for (String username : usernames) {
            String studentJson = getStudentJson(username);
            studentsMap.put(username, studentJson);
            mockStudent(username);
        }

        when(sharedPreferences.getAll()).thenAnswer(invocation -> studentsMap);
    }

    private void mockStudent(String username) {
        when(sharedPreferences.getString(username, null))
                .thenReturn(getStudentJson(username));
    }

    private String getStudentJson(String username) {
        Student student = getStudent(username);
        return gson.toJson(student);
    }

    private Student getStudent(String username) {
        Student student = new Student();
        student.username = username;
        return student;
    }
}
