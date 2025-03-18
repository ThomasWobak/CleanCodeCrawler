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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Crawler {
    private static final String FILEPATH ="C:\\Users\\thoma\\OneDrive\\Uni\\SS 25\\Clean Code\\Assignment1\\CrawlerCleanCode\\report.md";
    private static final int HEADINGNUMBERS=6;
    private static final int BADRESPONSECODES=400;
    private final Set<String> visitedUrls = new HashSet<>();
    private final StringBuilder markdownContent = new StringBuilder();
    private final int maxDepth;
    private final Set<String> allowedDomains;
    private final String startUrl;


    public Crawler(int maxDepth, Set<String> allowedDomains, String startUrl){
        this.maxDepth = maxDepth;
        this.allowedDomains = allowedDomains;
        this.startUrl=startUrl;
    }

    public void startCrawl() throws IOException {
        if(isValidLink(startUrl)&&isAllowedDomain(startUrl)){
            crawl(startUrl,0);
            saveToMarkdown(FILEPATH);
        }
    }

    public void crawl(String url, int depth) throws IOException {
        if (depth > maxDepth || visitedUrls.contains(url) || !isAllowedDomain(url)) {
            return;
        }
        String indent = "--> ".repeat(depth);
        try {
            Document document = Jsoup.connect(url).get();
            visitedUrls.add(url);
            markdownContent.append("\n").append(indent).append("depth: ").append(depth).append("\n");

            markdownContent.append(indent).append("# ").append(url).append("\n");
            ArrayList<String> headings=extractHeadings(document);
            logHeadings(headings,indent);
            ArrayList<String> links=extractLinks(document);
            for (String link: links) {
                if (!link.isEmpty() && isValidLink(link) && !visitedUrls.contains(link)) {
                    logCorrectLink(link, indent);
                    crawl(link, depth+1);
                } else if (!isValidLink(link)) {
                    logBrokenLink(link, indent);
                }
            }

        } catch (IOException e) {
            logBrokenLink(url, indent);
        }
    }
    private void logBrokenLink(String link, String indent) {
        markdownContent.append(indent).append("--> broken link <").append(link).append(">\n");
    }
    private void logCorrectLink(String link, String indent) {
        markdownContent.append(indent).append("--> link to <").append(link).append(">\n");
    }


    private ArrayList<String> extractLinks(Document document) throws IOException {
        ArrayList<String> allLinks= new ArrayList<>();
        Elements links = document.select("a[href]");
        for (Element link : links) {
            allLinks.add(link.absUrl("href"));
        }
        return allLinks;
    }
    private ArrayList<String> extractHeadings(Document document) {
        ArrayList<String> allHeadings= new ArrayList<>();
        for (int i = 1; i <= HEADINGNUMBERS; i++) {
            Elements headings = document.select("h" + i);
            for (Element heading : headings) {
                String currentHeading="#".repeat(i)+" "+heading.text();
                allHeadings.add(currentHeading);
            }
        }
        return allHeadings;
    }
    private void logHeadings(ArrayList<String> headings, String indent) {
        for (String heading: headings) {
            markdownContent.append(indent).append(heading).append("\n");
        }
    }

    public boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    public boolean isValidLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return false; // Ignore non-HTTP(S) links like mailto:, ftp:, etc.
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() < BADRESPONSECODES;
        } catch (IOException e) {
            return false;
        }
    }

    public void saveToMarkdown(String filePath) throws IOException {
        java.nio.file.Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        Files.write(path, markdownContent.toString().getBytes());
    }

}
