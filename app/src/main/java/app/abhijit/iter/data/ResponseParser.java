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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.text.WordUtils;

public class ResponseParser {

    private static JsonParser mJsonParser = new JsonParser();

    public static JsonObject parseLoginResponse(String loginResponse) throws JsonSyntaxException {
        JsonObject response;
        try {
            response = mJsonParser.parse(loginResponse).getAsJsonObject();
        } catch (Exception e) {
            throw new JsonSyntaxException("json parse error");
        }
        JsonObject login = new JsonObject();

        if (!response.has("name")) throw new JsonSyntaxException("name missing");
        login.addProperty("name", WordUtils.capitalizeFully(response.get("name").getAsString()));

        if (!response.has("status")) throw new JsonSyntaxException("status missing");
        login.add("status", response.get("status"));

        return login;
    }

    public static JsonObject parseAttendanceResponse(String attendanceResponse) throws JsonSyntaxException {
        JsonObject response;
        try {
            response = mJsonParser.parse(attendanceResponse).getAsJsonObject();
        } catch (Exception e) {
            throw new JsonSyntaxException("json parse error");
        }
        JsonArray responseSubjects = response.get("griddata").getAsJsonArray();
        JsonObject attendance = new JsonObject();

        for (int i = 0; i < responseSubjects.size(); i++) {
            JsonObject responseSubject = responseSubjects.get(i).getAsJsonObject();
            JsonObject subject = new JsonObject();

            if (!responseSubject.has("subject")) throw new JsonSyntaxException("name missing");
            subject.add("name", responseSubject.get("subject"));

            if (!responseSubject.has("subjectcode")) throw new JsonSyntaxException("code missing");
            subject.add("code", responseSubject.get("subjectcode"));

            if (!responseSubject.has("Latt")) throw new JsonSyntaxException("theory attendance missing");
            subject.add("theory", responseSubject.get("Latt"));

            if (!responseSubject.has("Patt")) throw new JsonSyntaxException("practical attendance missing");
            subject.add("practical", responseSubject.get("Patt"));

            attendance.add(subject.get("code").getAsString(), subject);
        }

        return attendance;
    }
}
