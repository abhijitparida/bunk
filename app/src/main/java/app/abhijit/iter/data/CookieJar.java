package app.abhijit.iter.data;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

class CookieJar implements okhttp3.CookieJar {

    private List<Cookie> cookieStore;

    CookieJar() {
        this.cookieStore = new ArrayList<>();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        this.cookieStore.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return this.cookieStore;
    }
}
