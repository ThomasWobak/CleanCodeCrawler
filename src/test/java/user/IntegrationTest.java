package user;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import crawler.Crawler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationTest {
    private static HttpServer server;
    private static final int PORT = 2000;
    private static String baseUrl;

    @BeforeAll
    static void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "<html><head><title>Test</title></head>"
                        + "<body><h1>Header</h1>"
                        + "<a href=\"/page2\">Page 2</a>"
                        + "</body></html>";
                if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()){
                        os.write(response.getBytes());
                    }
                }
            }
        });

        server.createContext("/page2", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "<html><head><title>Page2</title></head>"
                        + "<body><h2>Second Header</h2>"
                        + "<a href=\"/\">Back</a>"
                        + "</body></html>";
                if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()){
                        os.write(response.getBytes());
                    }
                }
            }
        });

        server.setExecutor(null);
        server.start();
        baseUrl = "http://localhost:" + PORT;
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stop(0);
        Files.deleteIfExists(Paths.get("reports/reports.md"));
    }

    @Test
    void testCrawlerIntegration() throws IOException {
        Crawler crawler = new Crawler(2, Collections.singleton("localhost"), baseUrl);
        crawler.startCrawl();

        String content = new String(Files.readAllBytes(Paths.get("reports/reports.md")));

        assertTrue(content.contains("Header"), "Markdown should contain the H1 header");
        assertTrue(content.contains("Second Header"), "Markdown should contain the H2 header from page2");
        assertTrue(content.contains("link to <" + baseUrl + ">"), "Markdown should log a link to the root page");
        assertTrue(content.contains("link to <" + baseUrl + "/page2>"), "Markdown should log a link to page2");
    }

    @Test
    void testCrawlerIntegrationInvalidLink() throws IOException {
        Crawler crawler = new Crawler(2, Collections.singleton("localhost"), baseUrl + ".jar");
        crawler.startCrawl();
        assertFalse(Files.exists(Paths.get("reports/reports.md")));
    }

    @Test
    void testCrawlerIntegrationInvalidDomain() throws IOException {
        Crawler crawler = new Crawler(2, Collections.singleton("localhost"), "http://localhorst:" + PORT);
        crawler.startCrawl();
        assertFalse(Files.exists(Paths.get("reports/reports.md")));
    }
}
