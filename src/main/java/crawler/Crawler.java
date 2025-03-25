package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Crawler {
    private static final String FILEPATH = "C:\\Users\\thoma\\OneDrive\\Uni\\SS 25\\Clean Code\\Assignment1\\CrawlerCleanCode\\report.md";
    private static final int INVALIDRESPONSECODES = 400;
    private final Set<String> visitedUrls = new HashSet<>();
    private final StringBuilder markdownContent = new StringBuilder();
    private final int maxDepth;
    private final Set<String> allowedDomains;
    private String currentUrl;
    private Document document;
    private Elements headings;
    private int currentDepth;
    private Elements links;

    public Crawler(int maxDepth, Set<String> allowedDomains, String startUrl) {
        this.maxDepth = maxDepth;
        this.allowedDomains = allowedDomains;
        this.currentUrl = startUrl;
        this.currentDepth = 0;
    }

    public void startCrawl() throws IOException {
        if (isValidLink(currentUrl) && isAllowedDomain(currentUrl)) {
            logCorrectLink(currentUrl);
            crawlLink(currentUrl, 0);
            saveToMarkdown();
        }
    }

    /***
     * Crawls a website, logging the headers and further links, then recursively crawls those links if they within the allowed domain
     * @param url current URL to crawl
     * @param depth current Depth in the crawl
     */
    protected void crawlLink(String url, int depth) {
        if (depth > maxDepth || visitedUrls.contains(url) || !isAllowedDomain(url)) {
            return;
        }
        this.currentDepth = depth;
        this.currentUrl = url;
        parse();
        cleanUrl();
        visitedUrls.add(currentUrl);
        logHeadings();
        for (Element currentLink : links) {
            String link = currentLink.absUrl("href");
            logLink(link);
            if (isCrawlable(link)) {
                crawlLink(link, depth + 1);
            }
        }
    }

    //Removes trailing /
    protected void cleanUrl() {
        if (currentUrl != null && currentUrl.endsWith("/")) {
            currentUrl = currentUrl.substring(0, currentUrl.length() - 1);
        }
    }

    protected void logLink(String link) {
        if (isValidLink(link)) {
            logCorrectLink(link);
        } else {
            logBrokenLink(link);
        }
    }

    protected void createDocument() {
        try {
            document = Jsoup.connect(currentUrl).get();

        } catch (IOException e) {
            System.err.println("Error connecting to " + currentUrl + "\n" + e.getMessage());
        }
    }

    protected void parse() {
        createDocument();
        extractHeadings();
        extractLinks();
    }

    protected void logBrokenLink(String link) {
        markdownContent.append(getIndent()).append("--> broken link <").append(link).append(">\n");
    }

    protected void logCorrectLink(String link) {
        markdownContent.append(getIndent()).append("--> link to <").append(link).append(">\n");
    }

    protected boolean isCrawlable(String link) {
        return (!link.isEmpty() && isValidLink(link) && !visitedUrls.contains(link));
    }

    protected void extractLinks() {
        links = document.select("a");
    }

    protected void extractHeadings() {
        headings = document.select("h1,h2,h3,h4,h5,h6");
    }

    protected void logHeadings() {
        for (Element heading : headings) {
            markdownContent.append(getIndent()).append(heading).append("\n");
        }
    }

    protected String getIndent() {
        return "--> ".repeat(currentDepth);
    }

    protected boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    protected boolean isValidLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.endsWith("jar")) {
                return false; // Ignore non-HTTP(S) links like mailto:, ftp:, etc.
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() < INVALIDRESPONSECODES;
        } catch (IOException e) {
            return false;
        }
    }

    protected void saveToMarkdown() throws IOException {
        java.nio.file.Path path = Paths.get(Crawler.FILEPATH);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, markdownContent.toString().getBytes());
        System.out.println("Saved to Markdown");
    }


}
