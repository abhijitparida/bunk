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

package app.abhijit.iter.data.source.remote;

import com.google.gson.JsonObject;

import app.abhijit.iter.util.Http;

public class IterApi {

    private static final String API_ENDPOINT = "http://111.93.164.202:8282/CampusLynxSOA/CounsellingRequest?refor=StudentOnlineDetailService";

    private static final String INSTITUTE_ID = "SOAUINSD1312A0000002";
    private static final String REGISTRATION_ID = "ITERRETD1606A0000001";

    public static String fetchStudentId(String registrationNumber) throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("sid", "validate");
        request.addProperty("instituteID", INSTITUTE_ID);
        request.addProperty("studentrollno", registrationNumber);
        return Http.post(API_ENDPOINT, "jdata=" + request.toString());
    }

    public static String fetchStudentDetails(String studentId) throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("sid", "studentdetails");
        request.addProperty("instituteid", INSTITUTE_ID);
        request.addProperty("studentid", studentId);
        return Http.post(API_ENDPOINT, "jdata=" + request.toString());
    }

    public static String fetchStudentSubjects(String studentId) throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("sid", "attendance");
        request.addProperty("instituteid", INSTITUTE_ID);
        request.addProperty("registrationid", REGISTRATION_ID);
        request.addProperty("studentid", studentId);
        return Http.post(API_ENDPOINT, "jdata=" + request.toString());
    }
}
