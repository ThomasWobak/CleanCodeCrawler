package crawler;

import logger.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import parser.Parser;
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
    private final Parser parser;

    public Crawler(int maxDepth, Set<String> allowedDomains) {
        this.maxDepth = maxDepth;
        this.parser=new Parser(allowedDomains);
    }

    public void startCrawl(List<String> startUrls) throws InterruptedException, IOException {
        List<CrawlNode> roots = new ArrayList<>();

        for (String url : startUrls) {
            if (parser.isValidLink(url) && parser.isAllowedDomain(url)) {
                CrawlNode root = new CrawlNode(url, "<a>" + url + "</a>", 0, maxDepth);
                roots.add(root);
                phaser.register();
                executor.submit(() -> crawlLink(root));
            } else {
                CrawlNode root = new CrawlNode(url, "<a>" + url + "</a>", 0, maxDepth);
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
        String cleanedUrl = parser.cleanUrl(node.url);
        if (node.depth > maxDepth || !parser.isAllowedDomain(cleanedUrl) || !visitedUrls.add(cleanedUrl)) {
            phaser.arriveAndDeregister();
            return;
        }
        System.out.println("Crawling link "+cleanedUrl);
        visitedUrls.add(node.url);
        Document doc;
        try {
            doc = parser.parseDocument(node.url);
            if(!parser.isValidLink(node.url)){
                throw new IOException("Invalid link");
            }
        } catch (IOException e) {
            logger.logBrokenLink(node, node.rawHtml);
            phaser.arriveAndDeregister();
            return;
        }
        if(parser.isValidLink(node.url)){
            logger.logHeadings(node, doc);
        }


        for (Element linkElem : doc.select("a[href]")) {
            String link = linkElem.absUrl("href");
            String rawChildHtml = linkElem.outerHtml();

            if (!parser.isValidLink(link)) {
                logger.logBrokenLink(node, rawChildHtml);
                continue;
            }

            if (parser.isCrawlable(link,visitedUrls)) {
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
}