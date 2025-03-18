package crawler;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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

    }
    private boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    private boolean isValidLink(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() < 400;
        } catch (IOException e) {
            return false;
        }
    }


}
