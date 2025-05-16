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
    private static final String FILEPATH = "C:\\Users\\thoma\\Desktop\\crawlerOutput\\reports.md";
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 2000;
    private static final int THREAD_POOL_SIZE = 200;

    private final Set<String> allowedDomains;
    private final int maxDepth;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final StringBuilder markdownContent = new StringBuilder();

    private final Phaser phaser = new Phaser(1);

    public Crawler(int maxDepth, Set<String> allowedDomains) {
        this.maxDepth = maxDepth;
        this.allowedDomains = allowedDomains;
    }

    public void startCrawl(List<String> startUrls) throws InterruptedException, IOException {
        List<CrawlNode> roots = new ArrayList<>();

        for (String url : startUrls) {
            if (isValidLink(url) && isAllowedDomain(url)) {
                CrawlNode root = new CrawlNode(url, "<a>" + url + "</a>", 1);
                roots.add(root);
                phaser.register(); // Register crawl task
                executor.submit(() -> crawlLink(root));
            } else {
                CrawlNode root = new CrawlNode(url, "<a>" + url + "</a>", 1);
                root.loggedLinks.add("<br>broken or disallowed root link " + root.rawHtml);
                roots.add(root);
            }
        }

        phaser.arriveAndAwaitAdvance(); // Wait for all crawl tasks to finish

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        saveToMarkdown(roots);
    }

    protected void crawlLink(CrawlNode node) {
        String cleanedUrl = cleanUrl(node.url);
        if (node.depth > maxDepth || !isAllowedDomain(cleanedUrl) || !visitedUrls.add(cleanedUrl)) {
            phaser.arriveAndDeregister(); // Done with this task
            return;
        }
        Document doc;
        try {
            doc = parseDocument(node.url);
        } catch (IOException e) {
            node.loggedLinks.add(getIndent(node.depth) + "broken link " + node.rawHtml);
            phaser.arriveAndDeregister(); // Done
            return;
        }

        for (Element heading : doc.select("h1,h2,h3,h4,h5,h6")) {
            node.headings.add(getHeadingLine(node.depth, heading));
        }

        for (Element linkElem : doc.select("a[href]")) {
            String link = linkElem.absUrl("href");
            String rawChildHtml = linkElem.outerHtml();
            String indent = getIndent(node.depth);

            if (!isValidLink(link)) {
                node.loggedLinks.add("<br>" + indent + "broken link " + rawChildHtml);
                continue;
            }

            if (isCrawlable(link)) {
                CrawlNode child = new CrawlNode(link, rawChildHtml, node.depth + 1);
                node.children.add(child);

                phaser.register(); // Register new task
                executor.submit(() -> crawlLink(child));
            }else{
                node.loggedLinks.add("<br>" + indent + "link to" + rawChildHtml);
            }
        }

        phaser.arriveAndDeregister(); // Done with this node
    }


    protected String getIndent(int depth) {
        return "-->".repeat(depth);
    }

    protected String getHeadingLine(int depth, Element heading) {
        return getIndent(depth)+"# ".repeat(getHeadingLevel(heading.tagName()))
                 + heading.text() + "\n";
    }

    protected int getHeadingLevel(String tagName) {
        try {
            return Integer.parseInt(tagName.substring(1)); // e.g., h2 -> 2
        } catch (Exception e) {
            return 1;
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
        if (url.endsWith("/")||url.endsWith("#")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
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
            connection.setConnectTimeout(TIMEOUTMILLISECONDS);
            connection.setReadTimeout(TIMEOUTMILLISECONDS);
            return connection.getResponseCode() < INVALIDRESPONSECODES;
        } catch (IOException e) {
            return false;
        }
    }

    protected void saveToMarkdown(List<CrawlNode> roots) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (CrawlNode root : roots) {
            renderTree(root, sb);
        }

        Files.createDirectories(Paths.get(FILEPATH).getParent());
        Files.write(Paths.get(FILEPATH), sb.toString().getBytes());
        System.out.println("Saved to Markdown");
    }



    protected void renderTree(CrawlNode node, StringBuilder out) {
        if(node.depth>maxDepth){
            for (String linkLog : node.loggedLinks) {
                out.append(getIndent(node.depth)).append(linkLog).append("\n");
            }
        }else{
            out.append(getIndent(node.depth)).append("\n<br>link to: ").append(node.rawHtml).append("\n");
            out.append(getIndent(node.depth)).append("<br>depth: ").append(node.depth).append("\n");
            for (String h : node.headings) {
                out.append(h);
            }
            for (CrawlNode child : node.children) {
                renderTree(child, out);  // recursive inline
            }
            out.append("\n");
        }
    }
}
