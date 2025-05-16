package crawler;

import logger.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import writer.Writer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private static final String FILEPATH = "C:\\Users\\thoma\\Desktop\\crawlerOutput\\reports.md";
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 2000;
    private static final int THREAD_POOL_SIZE = 200;

    private final Set<String> allowedDomains;
    private final int maxDepth;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Phaser phaser = new Phaser(1);
    private final Logger logger = new Logger();
    private final Writer writer = new Writer(FILEPATH);

    public Crawler(int maxDepth, Set<String> allowedDomains) {
        this.maxDepth = maxDepth;
        this.allowedDomains = allowedDomains;
    }

    public void startCrawl(List<String> startUrls) throws InterruptedException, IOException {
        List<CrawlNode> roots = new ArrayList<>();

        for (String url : startUrls) {
            if (isValidLink(url) && isAllowedDomain(url)) {
                CrawlNode root = new CrawlNode(url, "<a>" + url + "</a>", 1, maxDepth);
                roots.add(root);
                phaser.register();
                executor.submit(() -> crawlLink(root));
            } else {
                CrawlNode root = new CrawlNode(url, "<a>" + url + "</a>", 1, maxDepth);
                logger.logBrokenLink(root, root.rawHtml);
                roots.add(root);
            }
        }
        phaser.arriveAndAwaitAdvance();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        writer.saveToMarkdown(roots);
    }

    protected void crawlLink(CrawlNode node) {
        String cleanedUrl = cleanUrl(node.url);
        if (node.depth > maxDepth || !isAllowedDomain(cleanedUrl) || !visitedUrls.add(cleanedUrl)) {
            phaser.arriveAndDeregister();
            return;
        }
        Document doc;
        try {
            doc = parseDocument(node.url);
        } catch (IOException e) {
            logger.logBrokenLink(node, node.rawHtml);
            phaser.arriveAndDeregister();
            return;
        }

        logger.logHeadings(node, doc);

        for (Element linkElem : doc.select("a[href]")) {
            String link = linkElem.absUrl("href");
            String rawChildHtml = linkElem.outerHtml();

            if (!isValidLink(link)) {
                logger.logBrokenLink(node, rawChildHtml);
                continue;
            }

            if (isCrawlable(link)) {
                CrawlNode child = new CrawlNode(link, rawChildHtml, node.depth + 1, maxDepth);
                node.children.add(child);
                phaser.register();
                executor.submit(() -> crawlLink(child));
            } else {
                logger.logLink(node, rawChildHtml);
            }
        }
        phaser.arriveAndDeregister();
    }

    protected String cleanUrl(String url) {
        if (url.endsWith("/") || url.endsWith("#")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
    protected Document parseDocument(String url) throws IOException {
        return Jsoup.connect(url).timeout(TIMEOUTMILLISECONDS).get();
    }

    protected boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    protected boolean isCrawlable(String link) {
        return (!link.isEmpty() && !visitedUrls.contains(link) && isValidLink(link));
    }

    protected boolean isValidLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.endsWith("jar")) {
                return false;
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUTMILLISECONDS);
            connection.setReadTimeout(TIMEOUTMILLISECONDS);
            return connection.getResponseCode() < INVALIDRESPONSECODES;
        } catch (IOException e) {
            return false;
        }
    }
}
