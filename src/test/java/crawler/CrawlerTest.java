package crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CrawlerTest {
    private Crawler webCrawler;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        webCrawler = new Crawler(2, Set.of("sample.com", "example.com"), "https://example.com/");
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
        Path filePath = tempDir.resolve("report.md");
        webCrawler.saveToMarkdown(filePath.toString());

        assertTrue(Files.exists(filePath));

    }

    @Test
    void testSaveToMarkdownCreateFileFailed() {
        Path filePath = tempDir.resolve("report.md");

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(filePath)).thenReturn(false);
            filesMock.when(() -> Files.createFile(filePath)).thenThrow(new IOException("Create file failed"));
            IOException exception = assertThrows(IOException.class, () ->
                    webCrawler.saveToMarkdown(filePath.toString())
            );
            assertEquals("Create file failed", exception.getMessage());
        }
    }

    @Test
    void saveToMarkdownWriteToFileFailed() {
        Path filePath = tempDir.resolve("report.md");

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(filePath)).thenReturn(true);
            filesMock.when(() -> Files.write(eq(filePath), any(byte[].class)))
                    .thenThrow(new IOException("Write failed"));
            IOException exception = assertThrows(IOException.class, () ->
                    webCrawler.saveToMarkdown(filePath.toString())
            );
            assertEquals("Write failed", exception.getMessage());
        }
    }
}
