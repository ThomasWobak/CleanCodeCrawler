package crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrawlNode {
    public int maxDepth;
    public String url;
    public String rawHtml;
    public int depth;
    public List<String> headings = new ArrayList<>();
    public List<String> loggedLinks = new ArrayList<>();
    public List<CrawlNode> children = Collections.synchronizedList(new ArrayList<>());


    public CrawlNode(String url, String rawHtml, int depth, int maxDepth) {
        this.url = url;
        this.rawHtml = rawHtml;
        this.depth = depth;
        this.maxDepth = maxDepth;
    }

}

