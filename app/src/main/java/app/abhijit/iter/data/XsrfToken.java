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

import java.io.IOException;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class XsrfToken implements Interceptor {

    private CookieJar cookieJar;

    XsrfToken(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder request = chain.request().newBuilder();
        List<Cookie> cookies = this.cookieJar.loadForRequest(chain.request().url());
        for (Cookie cookie : cookies) {
            if (cookie.name().equals("XSRF-TOKEN")) {
                request.header("X-XSRF-TOKEN", cookie.value());
            }
        }

        return chain.proceed(request.build());
    }
}
