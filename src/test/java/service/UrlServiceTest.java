package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UrlServiceTest {
    @Spy
    UrlService urlService;
    @Mock
    private HttpURLConnection connection;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        urlService = null;
    }

    @Test
    void testNormalizeUrlNormalize() {
        Assertions.assertEquals("https://example.com/foo/bar", urlService.normalize(" HTTPS://Example.COM/foo/bar/ "));
    }

    @Test
    void testNormalizeUrlTrim() {
        Assertions.assertEquals("https://example.com/foo/bar", urlService.normalize(" https://example.com/foo/bar "));
    }

    @Test
    void testNormalizeUrlCollapseParent() {
        Assertions.assertEquals("http://example.com/a/c", urlService.normalize("http://EXAMPLE.com/a/b/../c"));
    }

    @Test
    void testNormalizeUrlStripSlash() {
        Assertions.assertEquals("http://example.com/a", urlService.normalize("http://example.com/a/"));
    }

    @Test
    void testNormalizeUrlTrimmedRaw() {
        Assertions.assertEquals("not a ::: url", urlService.normalize("  not a ::: url  "));
    }

    @Test
    void testIsValidUnsupportedSchemes() {
        assertFalse(urlService.isValid("ftp://example.com"));
        assertFalse(urlService.isValid("mailto:foo@bar.com"));
        assertFalse(urlService.isValid("example.com"));
    }

    @Test
    void isValidTrueResponseCodeUnder400() throws Exception {
        String url = "http://good";
        doReturn(connection).when(urlService).openConnection(url);
        when(connection.getResponseCode()).thenReturn(200);

        assertTrue(urlService.isValid(url));
        verify(connection).setRequestMethod("HEAD");
    }

    @Test
    void isValidFalseResponseCode400orAbove() throws Exception {
        String url = "http://bad";
        doReturn(connection).when(urlService).openConnection(url);
        when(connection.getResponseCode()).thenReturn(404);

        assertFalse(urlService.isValid(url));
    }

    @Test
    void isAllowedDomainTrueAnyDomainMatches() {
        Set<String> doms = Set.of("foo.com", "bar.org");
        assertTrue(urlService.isAllowedDomain("http://sub.foo.com/path", doms));
        assertTrue(urlService.isAllowedDomain("https://bar.org/x", doms));
    }

    @Test
    void isAllowedDomainFalseNoneMatches() {
        Set<String> doms = Set.of("example.net");
        assertFalse(urlService.isAllowedDomain("http://foo.com/", doms));
    }

    @Test
    void isCrawlableFalseNullOrEmpty() {
        assertFalse(urlService.isCrawlable(null, Collections.emptySet(), Collections.emptySet()));
        assertFalse(urlService.isCrawlable("",   Collections.emptySet(), Collections.emptySet()));
    }

    @Test
    void isCrawlableFalseVisited() {
        String u = "http://example";
        Set<String> visited = new HashSet<>(Set.of(u));
        doReturn(true).when(urlService).isValid(u);
        doReturn(true).when(urlService).isAllowedDomain(u, Collections.emptySet());

        assertFalse(urlService.isCrawlable(u, visited, Collections.emptySet()));
    }

    @Test
    void isCrawlableFalseInvalidOrNotAllowed() {
        String u = "http://foo";
        doReturn(false).when(urlService).isValid(u);
        assertFalse(urlService.isCrawlable(u, Collections.emptySet(), Set.of("foo")));

        doReturn(true).when(urlService).isValid(u);
        doReturn(false).when(urlService).isAllowedDomain(u, Set.of("bar"));
        assertFalse(urlService.isCrawlable(u, Collections.emptySet(), Set.of("bar")));
    }

    @Test
    void isCrawlableTrueAllChecksPass() {
        String u = "http://good";
        doReturn(true).when(urlService).isValid(u);
        doReturn(true).when(urlService).isAllowedDomain(u, Set.of("good"));
        assertTrue(urlService.isCrawlable(u, Collections.emptySet(), Set.of("good")));
    }
}
