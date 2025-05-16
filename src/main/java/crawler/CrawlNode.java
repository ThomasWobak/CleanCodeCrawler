package crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CrawlNode {
    String url;
    String rawHtml;
    int depth;
    List<String> headings = new ArrayList<>();
    List<String> loggedLinks = new ArrayList<>();
    List<CrawlNode> children = Collections.synchronizedList(new ArrayList<>());

    public CrawlNode(String url, String rawHtml, int depth) {
        this.url = url;
        this.rawHtml = rawHtml;
        this.depth = depth;
    }
}

