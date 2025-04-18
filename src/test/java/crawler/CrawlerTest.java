package crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CrawlerTest {
    private static final String FILE_PATH = "reports\\reports.md";
    private Crawler webCrawler;

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_PATH));
        webCrawler = new Crawler(2, Set.of("sample.com", "example.com"), "https://example.com/");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }

    @Test
    void testIsAllowedDomainValidDomain() {
        assertTrue(webCrawler.isAllowedDomain("https://www.sample.com/page"));
        assertTrue(webCrawler.isAllowedDomain("http://example.com/home"));
    }

    @Test
    void testIsAllowedDomainInvalidDomain() {
        assertFalse(webCrawler.isAllowedDomain("https://unauthorized.com"));
        assertFalse(webCrawler.isAllowedDomain("http://notallowed.net"));
    }

    @Test
    void testIsValidLinkValidURL() {
        assertTrue(webCrawler.isValidLink("https://google.com"));
        assertTrue(webCrawler.isValidLink("https://www.erpelstolz.at/gateway/formular-zentral.html"));
    }

    @Test
    void testIsValidLinkBadRequest() {
        try (MockedConstruction<URL> mocked = Mockito.mockConstruction(URL.class,
                (mock, context) -> {
                    HttpURLConnection mockConnection = Mockito.mock(HttpURLConnection.class);
                    when(mockConnection.getResponseCode()).thenReturn(400);
                    when(mock.openConnection()).thenReturn(mockConnection);
                })) {
            assertFalse(webCrawler.isValidLink("https://google.com"));
        }
    }

    @Test
    void testIsValidLinkInvalidURL() {
        assertFalse(webCrawler.isValidLink("htps://google.com"));
        assertFalse(webCrawler.isValidLink("localhost:4022"));
        assertFalse(webCrawler.isValidLink("Dashboard.jar"));
        assertFalse(webCrawler.isValidLink("http://dashboard.jar"));
    }

    @Test
    void testSaveToMarkdownSuccess() throws IOException {
        Path filePath = Paths.get(FILE_PATH);
        Files.deleteIfExists(filePath);
        Files.createDirectories(filePath.getParent());
        webCrawler.saveToMarkdown();
        assertTrue(Files.exists(filePath));
    }

    @Test
    void testSaveToMarkdownCreateFileFailed() {
        Path filePath = Paths.get(FILE_PATH);
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(filePath)).thenReturn(false);
            filesMock.when(() -> Files.createFile(filePath))
                    .thenThrow(new IOException("Create file failed"));
            IOException exception = assertThrows(IOException.class, () ->
                    webCrawler.saveToMarkdown()
            );
            assertEquals("Create file failed", exception.getMessage());
        }
    }

    @Test
    void testSaveToMarkdownWriteToFileFailed() {
        Path filePath = Paths.get(FILE_PATH);
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(filePath)).thenReturn(true);
            filesMock.when(() -> Files.write(eq(filePath), any(byte[].class)))
                    .thenThrow(new IOException("Write failed"));
            IOException exception = assertThrows(IOException.class, () ->
                    webCrawler.saveToMarkdown()
            );
            assertEquals("Write failed", exception.getMessage());
        }
    }

    @Test
    void testCleanURLSuccess() throws NoSuchFieldException, IllegalAccessException {
        webCrawler.cleanUrl();
        Field currentUrlField = Crawler.class.getDeclaredField("currentUrl");
        currentUrlField.setAccessible(true);
        assertEquals("https://example.com", currentUrlField.get(webCrawler));
    }

    @Test
    void testCleanURLNULLCheck() throws NoSuchFieldException, IllegalAccessException {
        Field currentUrlField = Crawler.class.getDeclaredField("currentUrl");
        currentUrlField.setAccessible(true);
        currentUrlField.set(webCrawler, null);
        webCrawler.cleanUrl();
        assertNull(currentUrlField.get(webCrawler));
    }

    @Test
    void testCleanURLNoTrailingSlash() throws NoSuchFieldException, IllegalAccessException {
        Field currentUrlField = Crawler.class.getDeclaredField("currentUrl");
        currentUrlField.setAccessible(true);
        currentUrlField.set(webCrawler, "https://example.com");
        webCrawler.cleanUrl();
        assertEquals("https://example.com", currentUrlField.get(webCrawler));
    }

    @Test
    void testIsCrawlableSuccess() {
        assertTrue(webCrawler.isCrawlable("https://example.com"));
    }

    @Test
    void testIsCrawlableEmptyString() {
        assertFalse(webCrawler.isCrawlable(""));
    }

    @Test
    void testIsCrawlableAlreadyCrawled() {
        webCrawler.markAsVisited("https://example.com");
        assertFalse(webCrawler.isCrawlable("https://example.com"));
    }

    @Test
    void testIsCrawlableInvalidLink() {
        assertFalse(webCrawler.isCrawlable("example.com"));
    }

    @Test
    void testCreateDocumentWithDummyDocument() throws Exception {
        Connection dummyConnection = mock(Connection.class);
        when(dummyConnection.get()).thenReturn(getDummyDocument());

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(dummyConnection);

            Field currentUrlField = getCrawlerField("currentUrl");
            currentUrlField.set(webCrawler, "https://example.com/");

            webCrawler.createDocument();

            Field documentField = getCrawlerField("document");
            Object documentValue = documentField.get(webCrawler);

            assertNotNull(documentValue);
            assertEquals("Dummy Title", ((Document) documentValue).title());
        }
    }

    @Test
    void testCrawlLinkWithInvalidSubLinkSuccess() throws Exception {
        Connection dummyConnection = mock(Connection.class);
        when(dummyConnection.get()).thenReturn(getDummyDocument());

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
            jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(dummyConnection);
            jsoupMock.when(() -> Jsoup.parse(anyString())).thenCallRealMethod();

            webCrawler.crawlLink("https://example.com/", 1);

            Field currentHeadings = getCrawlerField("headings");
            Field currentLinks = getCrawlerField("links");

            assertEquals(extractHeadings(getDummyDocument()).toString(), currentHeadings.get(webCrawler).toString());
            assertEquals(extractLinks(getDummyDocument()).toString(), currentLinks.get(webCrawler).toString());
        }
    }

    @Test
    void testCrawlLinkOverMaxDepth() throws Exception {
        Field currentMaxDepth = getCrawlerField("maxDepth");
        currentMaxDepth.set(webCrawler, 1);
        webCrawler.crawlLink("http://test.com", 2);
        assertEquals("https://example.com/", getCrawlerField("currentUrl").get(webCrawler));
    }

    @Test
    void testCrawlLinkAlreadyVisited() throws Exception {
        Set<String> visitedUrls = new HashSet<>();
        visitedUrls.add("https://test.com/");
        setVisitedUrlsField(visitedUrls);
        webCrawler.crawlLink("https://test.com/", 2);
        assertEquals("https://example.com/", getCrawlerField("currentUrl").get(webCrawler));
    }

    @Test
    void testCrawlLinkInvalidLink() throws Exception {
        webCrawler.crawlLink("Dashboard.jar", 1);
        assertEquals("https://example.com/", getCrawlerField("currentUrl").get(webCrawler));
    }

    private static Document getDummyDocument() {
        String html = "<html>" +
                "<head><title>Dummy Title</title></head>" +
                "<body>" +
                "<h1>Dummy Heading</h1>" +
                "<p>Dummy paragraph content.</p>" +
                "<a>Dummy Link</a>" +
                "</body>" +
                "</html>";
        return Jsoup.parse(html);
    }

    private static Elements extractHeadings(Document document) {
        return document.select("h1,h2,h3,h4,h5,h6");
    }

    private static Elements extractLinks(Document document) {
        return document.select("a");
    }

    private Field getCrawlerField(String fieldName) throws Exception {
        Field field = webCrawler.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private void setVisitedUrlsField(Set<String> visitedUrls) throws Exception {
        Field currentVisitedUrls = getCrawlerField("visitedUrls");
        currentVisitedUrls.setAccessible(true);
        currentVisitedUrls.set(webCrawler, visitedUrls);
    }
}
