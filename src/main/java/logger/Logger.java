package logger;

import crawler.CrawlNode;
import dto.HeadingInfo;
import java.util.List;

public class Logger {
    public void logHeadings(CrawlNode node, List<HeadingInfo> headings) {
        for (HeadingInfo h : headings) {
            node.headings.add(getHeadingLine(node.depth, h));
        }
    }

    public void logBrokenLink(CrawlNode node, String rawHtml) {
        node.loggedLinks.add("<br>" + getIndent(node.depth) + "broken link " + rawHtml);
    }

    public void logLink(CrawlNode node, String rawHtml) {
        node.loggedLinks.add("<br>" + getIndent(node.depth) + "link to " + rawHtml);
    }

    private String getIndent(int depth) {
        return "-->".repeat(depth);
    }

    private String getHeadingLine(int depth, HeadingInfo h) {
        return getIndent(depth) + "# ".repeat(h.level()) + h.text() + "\n";
    }
}
