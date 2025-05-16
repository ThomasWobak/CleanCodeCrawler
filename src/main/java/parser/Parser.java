package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class Parser {
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 2000;
    private final Set<String> allowedDomains;


    public Parser(Set<String> allowedDomains){
        this.allowedDomains=allowedDomains;
    }
    public String cleanUrl(String url) {
        if (url.endsWith("/") || url.endsWith("#")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
    public Document parseDocument(String url) throws IOException {
        return Jsoup.connect(url).timeout(TIMEOUTMILLISECONDS).get();
    }

    public boolean isAllowedDomain(String url) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    public boolean isCrawlable(String link, Set<String> visitedUrls) {
        return (!link.isEmpty() && !visitedUrls.contains(link) && isValidLink(link));
    }

    public boolean isValidLink(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.endsWith("jar")) {
                return false;
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUTMILLISECONDS);
            connection.setReadTimeout(TIMEOUTMILLISECONDS);
            return connection.getResponseCode() < INVALIDRESPONSECODES;
        } catch (IOException e) {
            return false;
        }
    }
}
