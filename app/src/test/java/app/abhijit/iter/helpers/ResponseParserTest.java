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

package app.abhijit.iter.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ResponseParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseLoginResponse_emptyResponse_throwsException() {
        thrown.expect(JsonSyntaxException.class);
        ResponseParser.parseLoginResponse("");
    }

    @Test
    public void parseLoginResponse_validResponse_parsesCorrectly() {
        String loginResponse = "{\"name\":\"NAME\",\"status\":\"success\"}";
        JsonObject login = ResponseParser.parseLoginResponse(loginResponse);

        assertEquals(login.get("name").getAsString(), "Name");
        assertEquals(login.get("status").getAsString(), "success");
    }

    @Test
    public void parseAttendanceResponse_emptyResponse_throwsException() {
        thrown.expect(JsonSyntaxException.class);
        ResponseParser.parseAttendanceResponse("");
    }

    @Test
    public void parseAttendanceResponse_validResponse_parsesCorrectly() {
        String attendanceResponse = "{\"griddata\":[{\"Latt\":\"10 / 10\",\"Patt\":\"Not Applicable\",\"subject\":\"Subject I\",\"subjectcode\":\"SUB001\"},{\"Latt\":\"Not Applicable\",\"Patt\":\"20 / 20\",\"subject\":\"Subject II\",\"subjectcode\":\"SUB002\"}]}";
        JsonObject attendance = ResponseParser.parseAttendanceResponse(attendanceResponse);

        JsonObject sub1 = attendance.get("SUB001").getAsJsonObject();
        assertEquals(sub1.get("name").getAsString(), "Subject I");
        assertEquals(sub1.get("code").getAsString(), "SUB001");
        assertEquals(sub1.get("theory").getAsString(), "10 / 10");
        assertEquals(sub1.get("practical").getAsString(), "Not Applicable");

        JsonObject sub2 = attendance.get("SUB002").getAsJsonObject();
        assertEquals(sub2.get("name").getAsString(), "Subject II");
        assertEquals(sub2.get("code").getAsString(), "SUB002");
        assertEquals(sub2.get("theory").getAsString(), "Not Applicable");
        assertEquals(sub2.get("practical").getAsString(), "20 / 20");
    }
}
