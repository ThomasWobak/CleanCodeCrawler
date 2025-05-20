package logger;

import crawler.CrawlNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Logger {
    public void logHeadings(CrawlNode node, Document doc) {
        for (Element heading : doc.select("h1,h2,h3,h4,h5,h6")) {
            node.headings.add(getHeadingLine(node.depth, heading));
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

    private String getHeadingLine(int depth, Element heading) {
        return getIndent(depth) + "# ".repeat(getHeadingLevel(heading.tagName()))
                + heading.text() + "\n";
    }

    private int getHeadingLevel(String tagName) {
        try {
            return Integer.parseInt(tagName.substring(1));
        } catch (Exception e) {
            return 1;
        }
    }

}
