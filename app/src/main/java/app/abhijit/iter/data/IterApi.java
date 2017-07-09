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

import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IterApi {

    /*
    private static final String BASE_URL = "http://111.93.164.203/CampusPortalSOA";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private OkHttpClient mOkHttpClient;
    private GetStudentTask mGetStudentTask;

    public IterApi() {
        CookieJar cookieJar = new CookieJar();
        XsrfToken xsrfTokenInterceptor = new XsrfToken(cookieJar);
        mOkHttpClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(xsrfTokenInterceptor)
                .build();
    }

    public void getStudent(String username, String password, Callback callback) {
        if (mGetStudentTask != null) {
            mGetStudentTask.cancel(true);
        }

        mGetStudentTask = new GetStudentTask(callback);
        mGetStudentTask.execute(username, password);
    }

    private void getSessionCookies() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .head()
                .build();

        mOkHttpClient.newCall(request).execute();
    }

    private JsonObject login(String username, String password) throws IOException, JsonSyntaxException {
        JsonObject postBody = new JsonObject();
        postBody.addProperty("username", username);
        postBody.addProperty("password", password);

        Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(RequestBody.create(MEDIA_TYPE_JSON, postBody.toString()))
                .build();

        Response response = mOkHttpClient.newCall(request).execute();

        return ResponseParser.parseLoginResponse(response.body().string());
    }

    private JsonObject getAttendance() throws IOException, JsonSyntaxException {
        String registrationId = "ITERRETD1612A0000002";

        JsonObject postBody = new JsonObject();
        postBody.addProperty("registerationid", registrationId);

        Request request = new Request.Builder()
                .url(BASE_URL + "/attendanceinfo")
                .post(RequestBody.create(MEDIA_TYPE_JSON, postBody.toString()))
                .build();

        Response response = mOkHttpClient.newCall(request).execute();

        return ResponseParser.parseAttendanceResponse(response.body().string());
    }

    public interface Callback {

        public void onData(JsonObject data);

        public void onError(String error);
    }

    private class GetStudentTask extends AsyncTask<String, Void, JsonObject> {

        private Callback mCallback;
        private String mError;

        public GetStudentTask(Callback callback) {
            mCallback = callback;
            mError = null;
        }

        @Override
        protected JsonObject doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            JsonObject result = new JsonObject();

            try {
                getSessionCookies();
            } catch (IOException e) {
                mError = "network error";
                return null;
            }

            JsonObject loginResponse;
            try {
                loginResponse = login(username, password);
            } catch (IOException e) {
                mError = "network error";
                return null;
            } catch (JsonSyntaxException e) {
                mError = "network response error";
                return null;
            }

            if (loginResponse.get("status").getAsString().equals("error")) {
                mError = "invalid credentials";
                return null;
            }

            result.add("name", loginResponse.get("name"));
            result.addProperty("registration_number", username);

            JsonObject attendanceResponse;
            try {
                attendanceResponse = getAttendance();
            } catch (IOException e) {
                mError = "network error";
                return null;
            } catch (JsonSyntaxException e) {
                mError = "network response error";
                return null;
            }

            result.add("attendance", attendanceResponse);

            return result;
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            if (mCallback == null) {
                return;
            }

            if (mError != null) {
                mCallback.onError(mError);
            } else {
                mCallback.onData(result);
            }
        }
    }
    */
}
