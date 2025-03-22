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
import java.util.HashSet;
import java.util.Set;

public class Crawler {
    private static final String FILEPATH ="C:\\Users\\thoma\\OneDrive\\Uni\\SS 25\\Clean Code\\Assignment1\\CrawlerCleanCode\\report.md";
    private static final int BADRESPONSECODES=400;
    private final Set<String> visitedUrls = new HashSet<>();
    private final StringBuilder markdownContent = new StringBuilder();
    private final int maxDepth;
    private final Set<String> allowedDomains;
    private String currentUrl;
    private Document document;
    private Elements headings;
    private Elements links;
    private String title;

    public Crawler(int maxDepth, Set<String> allowedDomains, String startUrl){
        this.maxDepth = maxDepth;
        this.allowedDomains = allowedDomains;
        this.currentUrl =startUrl;
    }

    public void startCrawl() throws IOException {
        if(isValidLink(currentUrl)&&isAllowedDomain(currentUrl)){
            logCorrectLink(currentUrl,"");
            crawl(currentUrl,0);
            saveToMarkdown(FILEPATH);
        }
    }

    /***
     * Crawls a website, logging the headers and further links, then recursively crawls those links if they within the allowed domain
     * @param url current URL to crawl
     * @param depth current Depth in the crawl
     * @throws IOException if the link doesn't work
     */
    public void crawl(String url, int depth) throws IOException {
        if (depth > maxDepth || visitedUrls.contains(url) || !isAllowedDomain(url)) {
            return;
        }
        this.currentUrl =url;
        String indent = "--> ".repeat(depth);
        parse();
        visitedUrls.add(url);
        try {
            logHeadings(indent);

            for (Element currentLink: links) {
                String link=currentLink.absUrl("href");
                if (checkCrawlable(link)) {
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
    private void createDocument(){
        try {
            document=Jsoup.connect(currentUrl).get();
            title=document.title();
        }catch (IOException e){
            System.err.println("Error connecting to "+ currentUrl + "\n"+e.getMessage());
        }
    }
    private void parse(){
        createDocument();
        extractHeadings();
        extractLinks();
    }

    private void logBrokenLink(String link, String indent) {
        markdownContent.append(indent).append("--> broken link <").append(link).append(">\n");
    }
    private void logCorrectLink(String link, String indent) {
        markdownContent.append(indent).append("--> link to <").append(link).append(">\n");
    }
    private boolean checkCrawlable(String link){
        return (!link.isEmpty() && isValidLink(link) && !visitedUrls.contains(link));
    }
    private void extractLinks() {
        links = document.select("a");
    }
    private void extractHeadings() {
        headings=document.select("h1,h2,h3,h4,h5,h6");
    }
    private void logHeadings( String indent) {
        for (Element heading: headings) {
            markdownContent.append(indent).append(heading).append("\n");
        }
    }

    public boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    public boolean isValidLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")&&!url.endsWith("jar")) {
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
        System.out.println("Saved to Markdown");
    }


}
