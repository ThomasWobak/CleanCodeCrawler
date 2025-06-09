package user;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import crawler.Crawler;
import dto.ParsedInputArguments;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {
    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
        registerContext("/",
                "<html><body><h1>Header</h1>"
                        + "<a href=\"/page2\">Page 2</a></body></html>");
        registerContext("/page2",
                "<html><body><h2>Second Header</h2>"
                        + "<a href=\"/\">Back</a></body></html>");

        server.setExecutor(null);
        server.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.stop(0);
        Files.deleteIfExists(Paths.get("report/report.md"));
    }

    @Test
    void testCrawlerIntegrationValidSiteProducesReport() throws IOException {
        ParsedInputArguments input = getInputArguments(baseUrl, 2, Set.of("localhost"));
        Crawler crawler = new Crawler(input);
        crawler.startCrawl();

        Path reportPath = Paths.get("report", "report.md");
        assertTrue(Files.exists(reportPath), "Report file should be created");

        String content = Files.readString(reportPath);
        assertTrue(content.contains("Header"), "Should contain H1 header");
        assertTrue(content.contains("Second Header"), "Should contain H2 header");
        assertTrue(content.contains("link to: <a>" + baseUrl + "</a>"),
                "Should log link to root page");
        assertTrue(content.contains("link to: <a href=\"/page2\">Page 2</a>"),
                "Should log link to page2");
    }

    @Test
    void testCrawlerIntegrationInvalidLinkBrokenLink() throws IOException {
        ParsedInputArguments input = getInputArguments(baseUrl + ".jar", 1, Set.of("localhost"));
        Crawler crawler = new Crawler(input);
        crawler.startCrawl();

        Path reportPath = Paths.get("report", "report.md");
        assertTrue(Files.exists(reportPath), "Report should still be created");
        String content = Files.readString(reportPath);
        assertTrue(content.contains("broken link <a>" + baseUrl + ".jar" + "</a>"));
    }

    @Test
    void testCrawlerIntegrationInvalidDomainBrokenLink() throws IOException {
        ParsedInputArguments input = getInputArguments(baseUrl, 1, Set.of("foo.com"));
        Crawler crawler = new Crawler(input);
        crawler.startCrawl();

        Path reportPath = Paths.get("report", "report.md");
        assertTrue(Files.exists(reportPath), "Report should still be created");
        String content = Files.readString(reportPath);
        assertTrue(content.contains("broken link <a>" + baseUrl + "</a>"));
    }

    private ParsedInputArguments getInputArguments(String startUrl, int maxDepth, Set<String> allowedDomains) {
        ParsedInputArguments input = new ParsedInputArguments();
        input.setStartUrl(startUrl);
        input.setMaxDepth(maxDepth);
        input.setAllowedDomains(allowedDomains);
        return input;
    }

    private static void registerContext(String path, String html) {
        server.createContext(path, new HtmlHandler(html));
    }

    /**
     * A tiny handler that replies 200 to HEAD, and serves the given HTML on GET.
     * Created with ChatGPT
     */
    private static class HtmlHandler implements HttpHandler {
        private final byte[] body;
        HtmlHandler(String html) {
            this.body = html.getBytes();
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        }
    }
}
