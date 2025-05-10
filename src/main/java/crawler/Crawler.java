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
import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private static final String FILEPATH = "C:\\Users\\thoma\\Desktop\\crawlerOutput\\report.md";
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 2000;
    private static final int THREAD_POOL_SIZE = 20;

    private final Set<String> allowedDomains;
    private final int maxDepth;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final StringBuilder markdownContent = new StringBuilder();

    public Crawler(int maxDepth, Set<String> allowedDomains) {
        this.maxDepth = maxDepth;
        this.allowedDomains = allowedDomains;
    }

    public void startCrawl(List<String> startUrls) throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(startUrls.size());

        for (String startUrl : startUrls) {
            if (isValidLink(startUrl) && isAllowedDomain(startUrl)) {
                executor.submit(() -> {
                    try {
                        crawlLink(startUrl, 0);
                    } finally {
                        latch.countDown();
                    }
                });
            } else {
                markdownContent.append("Root URL broken or not allowed: ").append(startUrl).append("\n");
                latch.countDown();
            }
        }
        latch.await();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        saveToMarkdown();
    }

    protected void crawlLink(String url, int depth) {
        String cleanedUrl = cleanUrl(url);
        visitedUrls.add(cleanedUrl);
        if (depth > maxDepth || visitedUrls.contains(cleanedUrl) || !isAllowedDomain(cleanedUrl)) return;

        Document doc;
        try {
             doc=parseDocument(cleanedUrl);
        } catch (IOException e) {
            logBrokenLink(cleanedUrl, depth);
            return;
        }
        logHeadings(doc, cleanedUrl, depth);
        Elements links = doc.select("a[href]");
        for (Element linkElem : links) {
            String link = linkElem.absUrl("href");
            logLink(link, depth);
            if (isCrawlable(link)) {
                executor.submit(() -> crawlLink(link, depth + 1));
            }
        }
    }

    protected Document parseDocument(String url) throws IOException {
        return Jsoup.connect(url).timeout(TIMEOUTMILLISECONDS).get();
    }
    protected boolean isCrawlable(String link) {
        return (!link.isEmpty() && !visitedUrls.contains(link) && isValidLink(link));
    }

    protected void logHeadings(Document doc, String url, int depth) {
        Elements headings = doc.select("h1,h2,h3,h4,h5,h6");
        String indent = "--> ".repeat(depth);
        synchronized (markdownContent) {
            markdownContent.append(indent).append("\n\nPage: ").append(url).append("\n");
            for (Element heading : headings) {
                markdownContent.append(indent).append(heading).append("\n");
            }
        }
    }

    protected void logLink(String link, int depth) {
        if (isValidLink(link)) {
            logCorrectLink(link, depth);
        } else {
            logBrokenLink(link, depth);
        }
    }

    protected void logBrokenLink(String link, int depth) {
        synchronized (markdownContent) {
            markdownContent.append("--> ".repeat(depth)).append("--> broken link <").append(link).append(">\n");
        }
    }

    protected void logCorrectLink(String link, int depth) {
        synchronized (markdownContent) {
            markdownContent.append("--> ".repeat(depth)).append("--> link to <").append(link).append(">\n");
        }
    }

    protected String cleanUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    protected boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    protected boolean isValidLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.endsWith("jar")) return false;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUTMILLISECONDS);
            connection.setReadTimeout(TIMEOUTMILLISECONDS);
            return connection.getResponseCode() < INVALIDRESPONSECODES;
        } catch (IOException e) {
            return false;
        }
    }

    protected void saveToMarkdown() throws IOException {
        java.nio.file.Path path = Paths.get(FILEPATH);
        Files.createDirectories(path.getParent());
        Files.write(path, markdownContent.toString().getBytes());
        System.out.println("Saved to Markdown");
    }
}
