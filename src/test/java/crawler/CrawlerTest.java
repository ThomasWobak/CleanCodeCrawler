package crawler;

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
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
    void testIsValidLinkValidURL()  {
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
    void testIsValidLinkInvalidURL()  {
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
    void testIsCrawlableSuccess()  {
        assertTrue(webCrawler.isCrawlable("https://example.com"));
    }

    @Test
    void testIsCrawlableEmptyString() {
        assertFalse(webCrawler.isCrawlable(""));
    }

    @Test
    void testIsCrawlableAlreadyCrawled()  {
        webCrawler.markAsVisited("https://example.com");
        assertFalse(webCrawler.isCrawlable("https://example.com"));
    }

    @Test
    void testIsCrawlableInvalidLink() {
        assertFalse(webCrawler.isCrawlable("example.com"));
    }
}
