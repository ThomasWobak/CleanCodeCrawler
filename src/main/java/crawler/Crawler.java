package crawler;

import logger.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import parser.ParsedInputArguments;
import parser.LinkParser;
import writer.Writer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private static final String FILEPATH = "C:\\Users\\thoma\\Desktop\\crawlerOutput\\reports.md";

    private static final int THREAD_POOL_SIZE = 200;
    private final int maxDepth;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Phaser phaser = new Phaser(1);
    private final Logger logger = new Logger();
    private final Writer writer = new Writer(FILEPATH);
    private final LinkParser linkParser = new LinkParser();
    private final Set<String> allowedDomains;
    private final String startUrl;

    public Crawler(ParsedInputArguments inputArguments) {
        this.maxDepth = inputArguments.getMaxDepth();
        this.allowedDomains = inputArguments.getAllowedDomains();
        this.startUrl = inputArguments.getStartUrl();

    }

    public void startCrawl() throws InterruptedException, IOException {
        List<CrawlNode> roots = new ArrayList<>();


        if (linkParser.isValidLink(startUrl) && linkParser.isAllowedDomain(startUrl, allowedDomains)) {
            CrawlNode root = new CrawlNode(startUrl, "<a>" + startUrl + "</a>", 0, maxDepth);
            roots.add(root);
            phaser.register();
            executor.submit(() -> crawlLink(root));

        } else {
            CrawlNode root = new CrawlNode(startUrl, "<a>" + startUrl + "</a>", 0, maxDepth);
            logger.logBrokenLink(root, root.rawHtml);
            roots.add(root);
        }

        phaser.arriveAndAwaitAdvance();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        writer.saveToMarkdown(roots);
    }

    protected void crawlLink(CrawlNode node) {
        String cleanedUrl = linkParser.cleanUrl(node.url);

        if (node.depth > maxDepth || !linkParser.isAllowedDomain(cleanedUrl, allowedDomains) || !visitedUrls.add(cleanedUrl) || !visitedUrls.add(node.rawHtml)) {
            phaser.arriveAndDeregister();
            return;
        }

        Document doc;
        try {
            doc = linkParser.parseDocument(node.url);
            if (!linkParser.isValidLink(node.url)) {
                throw new IOException("Invalid link");
            }
            if (linkParser.isValidLink(node.url)) {
                logger.logHeadings(node, doc);
            }


            for (Element linkElem : doc.select("a[href]")) {
                String link = linkElem.absUrl("href");
                String rawChildHtml = linkElem.outerHtml();

                if (!linkParser.isValidLink(link)) {
                    logger.logBrokenLink(node, rawChildHtml);
                    continue;
                }
                if (linkParser.isCrawlable(link, visitedUrls)) {
                    CrawlNode child = new CrawlNode(link, rawChildHtml, node.depth + 1, maxDepth);
                    node.children.add(child);
                    phaser.register();
                    executor.submit(() -> crawlLink(child));
                } else {
                    logger.logLink(node, rawChildHtml);
                }
            }
        } catch (IOException e) {
            logger.logBrokenLink(node, node.rawHtml);
            phaser.arriveAndDeregister();
            return;
        }
        phaser.arriveAndDeregister();
    }
}