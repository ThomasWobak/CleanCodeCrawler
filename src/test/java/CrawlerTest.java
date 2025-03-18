
import crawler.Crawler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CrawlerTest {
    private Crawler webCrawler;

    @BeforeEach
    void setUp() {
        webCrawler = new Crawler(2, Set.of("sample.com", "example.com"), "https://example.com/");
    }

    @Test
    void testIsAllowedDomain_ValidDomain() {
        assertTrue(webCrawler.isAllowedDomain("https://www.sample.com/page"));
        assertTrue(webCrawler.isAllowedDomain("http://example.com/home"));
    }

    @Test
    void testIsAllowedDomain_InvalidDomain() {
        assertFalse(webCrawler.isAllowedDomain("https://unauthorized.com"));
        assertFalse(webCrawler.isAllowedDomain("http://notallowed.net"));
    }

    @Test
    void testIsValidLink_ValidURL() throws IOException {
        assertTrue(webCrawler.isValidLink("https://google.com"));
    }

    @Test
    void testIsValidLink_BrokenURL() throws IOException {
        assertFalse(webCrawler.isValidLink("htps://google.com"));
    }
}
