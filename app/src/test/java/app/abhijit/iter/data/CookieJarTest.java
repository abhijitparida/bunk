package app.abhijit.iter.data;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import okhttp3.Cookie;
import org.junit.Before;
import org.junit.Test;

public class CookieJarTest {

    private CookieJar cookieJar;

    @Before
    public void setUp() {
        cookieJar = new CookieJar();
    }

    @Test
    public void saveFromResponse_ValidCookieList_PopulatesCookieStoreAccordingly() {
        cookieJar.saveFromResponse(null, createCookies());
        List<Cookie> cookieStore = cookieJar.loadForRequest(null);

        assertEquals(1, cookieStore.size());
    }

    @Test
    public void loadForRequest_InitialState_ReturnEmptyCookieStore() {
        List<Cookie> cookieStore = cookieJar.loadForRequest(null);

        assertTrue(cookieStore.isEmpty());
    }

    private List<Cookie> createCookies() {
        return Collections.singletonList(mock(Cookie.class));
    }
}
