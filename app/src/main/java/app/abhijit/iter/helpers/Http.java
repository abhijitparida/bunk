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

package app.abhijit.iter.helpers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Deprecated
public class Http {

    private static final int TIMEOUT = 5000;
    private static final String USER_AGENT = System.getProperty("http.agent");

    public static String post(String url, String data) throws Exception {
        URL endPoint = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT == null ? "" : USER_AGENT);
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setDoOutput(true);
        DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
        outStream.writeBytes(data);
        outStream.flush();
        outStream.close();
        BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inStreamLine;
        StringBuilder response = new StringBuilder();
        while ((inStreamLine = inStream.readLine()) != null) {
            response.append(inStreamLine);
        }
        inStream.close();
        return response.toString();
    }

    public static String get(String url) throws Exception {
        URL endPoint = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setRequestProperty("User-Agent", USER_AGENT == null ? "" : USER_AGENT);
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inStreamLine;
        StringBuilder response = new StringBuilder();
        while ((inStreamLine = inStream.readLine()) != null) {
            response.append(inStreamLine);
        }
        inStream.close();
        return response.toString();
    }
}
