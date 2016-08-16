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

package app.abhijit.iter.data.source;

import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import app.abhijit.iter.BuildConfig;
import app.abhijit.iter.data.model.Telemetry;
import app.abhijit.iter.data.source.remote.TelemetryApi;

public class TelemetryDataSource {

    private String mLastRegistrationNumber;
    private String mLastResponse;

    public void fetch(String registrationNumber) {
        if (StringUtils.equals(registrationNumber, mLastRegistrationNumber)) {
            processTelemetry(mLastResponse);
        } else {
            new FetchTelemetryAsyncTask().execute(registrationNumber);
        }
    }

    public void processTelemetry(String response) {
        try {
            JsonObject telemetry = new JsonParser().parse(response).getAsJsonObject();
            boolean updateAvailable = telemetry.get("update").getAsBoolean();
            boolean displayAds = telemetry.get("ads").getAsBoolean();
            EventBus.getDefault().post(new Telemetry(updateAvailable, displayAds));
        } catch (Exception ignored) {
        }
    }

    private class FetchTelemetryAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String registrationNumber = params[0];
            String version = BuildConfig.VERSION_NAME;
            try {
                String response = TelemetryApi.fetchTelemetryData(registrationNumber, version);
                mLastRegistrationNumber = registrationNumber;
                mLastResponse = response;
                return response;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            processTelemetry(response);
        }
    }
}
