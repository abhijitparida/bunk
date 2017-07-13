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

package app.abhijit.iter.data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import app.abhijit.iter.exceptions.InvalidCredentialsException;
import app.abhijit.iter.exceptions.InvalidResponseException;
import app.abhijit.iter.models.Student;
import app.abhijit.iter.models.Subject;

import static org.junit.Assert.assertEquals;

public class ResponseParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parse_EmptyLoginJson_ThrowsInvalidResponseException() {
        thrown.expect(InvalidResponseException.class);

        ResponseParser responseParser = new ResponseParser();
        responseParser.parse("", "");
    }

    @Test
    public void parse_InvalidLoginJson_ThrowsInvalidResponseException() {
        thrown.expect(InvalidResponseException.class);

        ResponseParser responseParser = new ResponseParser();
        responseParser.parse("bad json", "");
    }

    @Test
    public void parse_StatusError_ThrowsInvalidCredentialsException() {
        thrown.expect(InvalidCredentialsException.class);

        ResponseParser responseParser = new ResponseParser();
        responseParser.parse("{\"name\":\"NAME\",\"status\":\"error\"}", "");
    }

    @Test
    public void parse_StatusSuccess_ProperlyCapitalizesStudentName() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parse("{\"name\":\"fIrStNaMe LASTNAME\",\"status\":\"success\"}", "");

        assertEquals("Firstname Lastname", student.name);
    }

    @Test
    public void parse_EmptyAttendanceJson_ReturnsStudentWithEmptySubjectList() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parse("{\"name\":\"name\",\"status\":\"success\"}", "");

        assertEquals(0, student.subjects.size());
    }

    @Test
    public void parse_InvalidAttendanceJson_ReturnsStudentWithEmptySubjectList() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parse("{\"name\":\"name\",\"status\":\"success\"}", "bad json");

        assertEquals(0, student.subjects.size());
    }

    @Test
    public void parse_ValidAttendanceJson_ParsesCorrectly() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parse("{\"name\":\"name\",\"status\":\"success\"}",
                "{\"griddata\":[{\"Latt\":\"10 / 10\",\"Patt\":\"Not Applicable\",\"subject\":\"Subject I\",\"subjectcode\":\"SUB001\"},{\"Latt\":\"Not Applicable\",\"Patt\":\"20 / 20\",\"subject\":\"Subject II\",\"subjectcode\":\"SUB002\"}]}");
        HashMap<String, Subject> subjects = student.subjects;

        assertEquals(2, subjects.size());

        assertEquals("Subject I", subjects.get("SUB001").name);
        assertEquals("SUB001", subjects.get("SUB001").code);
        assertEquals(0, subjects.get("SUB001").theoryPresent);
        assertEquals(0, subjects.get("SUB001").theoryTotal);
        assertEquals(10, subjects.get("SUB001").labPresent);
        assertEquals(10, subjects.get("SUB001").labTotal);

        assertEquals("Subject II", subjects.get("SUB002").name);
        assertEquals("SUB002", subjects.get("SUB002").code);
        assertEquals(20, subjects.get("SUB002").theoryPresent);
        assertEquals(20, subjects.get("SUB002").theoryTotal);
        assertEquals(0, subjects.get("SUB002").labPresent);
        assertEquals(0, subjects.get("SUB002").labTotal);
    }
}
