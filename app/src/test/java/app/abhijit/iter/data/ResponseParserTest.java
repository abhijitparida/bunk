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
    public void parseStudent_EmptyLoginJson_ThrowsInvalidResponseException() {
        thrown.expect(InvalidResponseException.class);

        ResponseParser responseParser = new ResponseParser();
        responseParser.parseStudent("", "");
    }

    @Test
    public void parseStudent_InvalidLoginJson_ThrowsInvalidResponseException() {
        thrown.expect(InvalidResponseException.class);

        ResponseParser responseParser = new ResponseParser();
        responseParser.parseStudent("bad json", "");
    }

    @Test
    public void parseStudent_StatusError_ThrowsInvalidCredentialsException() {
        thrown.expect(InvalidCredentialsException.class);

        ResponseParser responseParser = new ResponseParser();
        responseParser.parseStudent("{\"name\":\"NAME\",\"status\":\"error\"}", "");
    }

    @Test
    public void parseStudent_StatusSuccess_ProperlyCapitalizesStudentName() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parseStudent("{\"name\":\"fIrStNaMe LASTNAME\",\"status\":\"success\"}", "");

        assertEquals("Firstname Lastname", student.name);
    }

    @Test
    public void parseStudent_EmptyAttendanceJson_ReturnsStudentWithEmptySubjectList() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parseStudent("{\"name\":\"name\",\"status\":\"success\"}", "");

        assertEquals(0, student.subjects.size());
    }

    @Test
    public void parseStudent_InvalidAttendanceJson_ReturnsStudentWithEmptySubjectList() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parseStudent("{\"name\":\"name\",\"status\":\"success\"}", "bad json");

        assertEquals(0, student.subjects.size());
    }

    @Test
    public void parseStudent_ValidAttendanceJson_ParsesCorrectly() {
        ResponseParser responseParser = new ResponseParser();
        Student student = responseParser.parseStudent("{\"name\":\"name\",\"status\":\"success\"}",
                "{\"griddata\":[{\"Latt\":\"10 / 10\",\"Patt\":\"Not Applicable\",\"subject\":\"Subject I\",\"subjectcode\":\"SUB001\"},{\"Latt\":\"Not Applicable\",\"Patt\":\"20 / 20\",\"subject\":\"Subject II\",\"subjectcode\":\"SUB002\"}]}");
        HashMap<String, Subject> subjects = student.subjects;

        assertEquals(2, subjects.size());

        assertEquals("Subject I", subjects.get("SUB001").name);
        assertEquals("SUB001", subjects.get("SUB001").code);
        assertEquals(10, subjects.get("SUB001").theoryPresent);
        assertEquals(10, subjects.get("SUB001").theoryTotal);
        assertEquals(0, subjects.get("SUB001").labPresent);
        assertEquals(0, subjects.get("SUB001").labTotal);

        assertEquals("Subject II", subjects.get("SUB002").name);
        assertEquals("SUB002", subjects.get("SUB002").code);
        assertEquals(0, subjects.get("SUB002").theoryPresent);
        assertEquals(0, subjects.get("SUB002").theoryTotal);
        assertEquals(20, subjects.get("SUB002").labPresent);
        assertEquals(20, subjects.get("SUB002").labTotal);
    }

    @Test
    public void parseRegistrationId_ValidRegistrationIdJson_ParsesCorrectly() {
        ResponseParser responseParser = new ResponseParser();
        String registrationId = responseParser.parseRegistrationId("{\"studentdata\": [{\"REGISTRATIONID\": \"2018 ODD SEM - ITER\",\"REGISTRATIONDATEFROM\": 1528223400000},{\"REGISTRATIONID\": \"2018 EVEN SEMESTER\",\"REGISTRATIONDATEFROM\": 1511721000000},{\"REGISTRATIONID\": \"2017 ODD SEMESTER\",\"REGISTRATIONDATEFROM\": 1498847400000}]}");

        assertEquals("2018 ODD SEM - ITER", registrationId);
    }

    @Test
    public void parseRegistrationId_ValidRegistrationIdJson_ReturnsLatestRegistrationId() {
        ResponseParser responseParser = new ResponseParser();
        String registrationId = responseParser.parseRegistrationId("{\"studentdata\": [{\"REGISTRATIONID\": \"bbb\",\"REGISTRATIONDATEFROM\": 2},{\"REGISTRATIONID\": \"ccc\",\"REGISTRATIONDATEFROM\": 3},{\"REGISTRATIONID\": \"aaa\",\"REGISTRATIONDATEFROM\": 1}]}");

        assertEquals("ccc", registrationId);
    }
}
