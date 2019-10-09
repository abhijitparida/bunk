package app.abhijit.iter.data;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Formatter;

import app.abhijit.iter.R;
import app.abhijit.iter.exceptions.ConnectionFailedException;
import app.abhijit.iter.exceptions.InvalidCredentialsException;
import app.abhijit.iter.exceptions.InvalidResponseException;
import app.abhijit.iter.models.Student;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * This class fetches data from the API.
 */
public class IterApi {

    private String baseUrl;

    public IterApi(Context context) {
        this.baseUrl = context.getResources().getString(R.string.api_base_url);
    }

    public void getStudent(@NonNull String username, @NonNull String password, @NonNull Callback callback) {
        new FetchApiResponse().execute(username, password, callback, this.baseUrl);
    }

    public interface Callback {

        void onData(@NonNull Student student);

        void onError(@NonNull RuntimeException error);
    }

    private static class FetchApiResponse extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Object... params) {
            String username = (String) params[0];
            String password = (String) params[1];
            Callback callback = (Callback) params[2];
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cookieJar(new CookieJar())
                    .build();
            String baseUrl = (String) params[3];
            MediaType json = MediaType.parse("application/json");
            ResponseParser responseParser = new ResponseParser();

            try {
                String loginRequestBody = new Formatter()
                        .format("{\"username\":\"%s\",\"password\":\"%s\",\"MemberType\":\"S\"}",
                                username, password).toString();
                Request loginRequest = new Request.Builder()
                        .url(baseUrl + "/login")
                        .post(RequestBody.create(json, loginRequestBody))
                        .build();
                String loginJson = okHttpClient.newCall(loginRequest)
                        .execute().body().string();

                Request registrationIdRequest = new Request.Builder()
                        .url(baseUrl + "/studentSemester/lov")
                        .post(RequestBody.create(json, ""))
                        .build();
                String registrationIdJson = okHttpClient.newCall(registrationIdRequest)
                        .execute().body().string();
                String registrationId = responseParser.parseRegistrationId(registrationIdJson);

                String attendanceRequestBody = new Formatter()
                        .format("{\"registerationid\":\"%s\"}", registrationId).toString();
                Request attendanceRequest = new Request.Builder()
                        .url(baseUrl + "/attendanceinfo")
                        .post(RequestBody.create(json, attendanceRequestBody))
                        .build();
                String attendanceJson = okHttpClient.newCall(attendanceRequest)
                        .execute().body().string();

                Student student = responseParser.parseStudent(loginJson, attendanceJson);
                student.setUsername(username);
                student.setPassword(password);
                return new Object[]{student, null, callback};
            } catch (InvalidCredentialsException | InvalidResponseException e) {
                return new Object[]{null, e, callback};
            } catch (Exception e) {
                return new Object[]{null, new ConnectionFailedException(), callback};
            }
        }

        @Override
        protected void onPostExecute(Object[] result) {
            Student student = (Student) result[0];
            RuntimeException error = (RuntimeException) result[1];
            Callback callback = (Callback) result[2];

            if (callback != null) {
                if (error != null) {
                    callback.onError(error);
                } else {
                    callback.onData(student);
                }
            }
        }
    }
}
