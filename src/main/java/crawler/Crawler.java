package crawler;

import dto.LinkInfo;
import dto.Page;
import exception.crawler.CrawlerException;
import exception.parser.ParserException;
import logger.Logger;
import parser.JsoupParser;
import dto.ParsedInputArguments;
import parser.Parser;
import service.UrlService;
import writer.Writer;

import java.util.*;
import java.util.concurrent.*;

public class Crawler {
    private static final String FILEPATH = "report\\report.md";

    private static final int THREAD_POOL_SIZE = 100;
    private final int maxDepth;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final Phaser phaser = new Phaser(1);
    private final Logger logger = new Logger();
    private final Writer writer = Writer.create(FILEPATH);
    private final Parser parser = new JsoupParser();
    private final UrlService urlService = new UrlService();
    private final Set<String> allowedDomains;
    private final String startUrl;

    public Crawler(ParsedInputArguments inputArguments) {
        this.maxDepth = inputArguments.getMaxDepth();
        this.allowedDomains = inputArguments.getAllowedDomains();
        this.startUrl = inputArguments.getStartUrl();
    }

    public void startCrawl() {
        List<CrawlNode> roots = new ArrayList<>();
        try {
            String rootUrl = urlService.normalize(startUrl);
            if (!urlService.isCrawlable(rootUrl, visitedUrls, allowedDomains)) {
                CrawlNode badRoot = new CrawlNode(startUrl, "<a>" + startUrl + "</a>", 0, maxDepth);
                logger.logBrokenLink(badRoot, badRoot.rawHtml);
                roots.add(badRoot);
                return;
            }

            CrawlNode root = new CrawlNode(rootUrl, "<a>" + rootUrl + "</a>", 0, maxDepth);
            roots.add(root);
            phaser.register();
            executor.submit(() -> crawlLink(root));

            phaser.arriveAndAwaitAdvance();
        } catch (Exception e) {
            throw new CrawlerException("Fatal error in startCrawl()", e);
        } finally {
            shutdownExecutor();
            writeReport(roots);
        }
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

        } catch (ParserException e) {
            logger.logBrokenLink(node, node.rawHtml);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    private void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.logError("Interrupted while shutting down crawler threads", ie);
        }
    }

    private void writeReport(List<CrawlNode> roots) {
        try {
            writer.saveToMarkdown(roots);
        } catch (Exception writeErr) {
            logger.logError("Failed to write crawl report", writeErr);
        }
    }
}