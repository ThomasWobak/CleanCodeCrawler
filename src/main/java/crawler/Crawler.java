package crawler;
import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Crawler {
    private static final String FILEPATH ="C:\\Users\\thoma\\OneDrive\\Uni\\SS 25\\Clean Code\\Assignment1\\CrawlerCleanCode\\src\\main\\java\\crawler\\test.md";
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
            markdownContent.append("\n" + indent + "depth: " + depth + "\n");
            markdownContent.append(indent + "# " + url + "\n");
            extractHeadings(document, depth, indent);
            extractLinks(document, url, depth, indent);
            saveToMarkdown(FILEPATH);

        } catch (IOException e) {
            System.out.println("ERROR "+e.getMessage());
            markdownContent.append("\n" + indent + "--> broken link <" + url + ">\n");
            saveToMarkdown(FILEPATH);

        }
    }

    private void extractLinks(Document document, String parentUrl, int depth, String indent) throws IOException {
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absUrl = link.absUrl("href");
            if (!absUrl.isEmpty() && isValidLink(absUrl) && !visitedUrls.contains(absUrl)) {
                markdownContent.append(indent).append("--> link to <").append(absUrl).append(">\n");

            } else if (!isValidLink(absUrl)) {
                markdownContent.append(indent).append("--> broken link <").append(absUrl).append(">\n");
            }
        }
    }
    private void extractHeadings(Document document, int depth, String indent) {
        for (int i = 1; i <= 6; i++) {
            Elements headings = document.select("h" + i);
            for (Element heading : headings) {
                markdownContent.append(indent).append("#".repeat(i)).append(" ").append(heading.text()).append("\n");
            }
        }
    }

    public boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    public boolean isValidLink(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() < 400;
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
