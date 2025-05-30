package crawler;

import dto.LinkInfo;
import dto.Page;
import logger.Logger;
import parser.ParsedInputArguments;
import parser.Parser;
import service.UrlService;
import writer.Writer;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private static final String FILEPATH = "reports\\report.md";

    private static final int THREAD_POOL_SIZE = 100;
    private final int maxDepth;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Phaser phaser = new Phaser(1);
    private final Logger logger = new Logger();
    private final Writer writer = new Writer(FILEPATH);
    private final Parser parser;
    private final UrlService urlService = new UrlService();
    private final Set<String> allowedDomains;
    private final String startUrl;

    public Crawler(ParsedInputArguments inputArguments, Parser parser) {
        this.maxDepth = inputArguments.getMaxDepth();
        this.allowedDomains = inputArguments.getAllowedDomains();
        this.startUrl = inputArguments.getStartUrl();
        this.parser = parser;
    }

    public void startCrawl() throws InterruptedException, IOException {
        List<CrawlNode> roots = new ArrayList<>();
        String rootUrl = urlService.normalize(startUrl);
        if (urlService.isCrawlable(rootUrl, visitedUrls, allowedDomains)) {
            CrawlNode root = new CrawlNode(rootUrl, "<a>" + rootUrl + "</a>", 0, maxDepth);
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
        String normalizedUrl = urlService.normalize(node.url);
        if (!visitedUrls.add(normalizedUrl)) {
            phaser.arriveAndDeregister();
            return;
        }
        try {
            Page page = parser.parsePage(node.url);
            logger.logHeadings(node, page.getHeadings());
            for (LinkInfo link : page.getOutgoingLinks()) {
                String rawChildHtml = link.rawHtml();
                String candidate = link.href();

                if (!urlService.isValid(candidate)) {
                    logger.logBrokenLink(node, rawChildHtml);
                }

                String normalizedChild = urlService.normalize(candidate);
                if (node.depth + 1 <= maxDepth
                        && urlService.isAllowedDomain(normalizedChild, allowedDomains)
                        && urlService.isValid(candidate)
                        && !visitedUrls.contains(normalizedChild)) {

                    CrawlNode child = new CrawlNode(candidate, rawChildHtml, node.depth + 1, maxDepth);
                    node.children.add(child);
                    phaser.register();
                    executor.submit(() -> crawlLink(child));
                } else {
                    logger.logLink(node, rawChildHtml);
                }
            }

        } catch (IOException e) {
            logger.logBrokenLink(node, node.rawHtml);
        } finally {
            phaser.arriveAndDeregister();
        }
    }
}