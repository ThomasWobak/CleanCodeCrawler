package parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class LinkParser {
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 20000;


    public String cleanUrl(String url) {
        if (url.endsWith("/") || url.endsWith("#")) {
            return url.substring(0, url.length() - 1).trim();
        }
        return url.trim();
    }

    public Document parseDocument(String url) throws IOException {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(TIMEOUTMILLISECONDS)
                    .userAgent("Mozilla/5.0 (compatible; GileadCrawler/1.0)")
                    .followRedirects(true)
                    .execute();

            int status = response.statusCode();
            if (status != 200) {
                throw new IOException("Received status " + status);
            }

            return response.parse();
        } catch (IOException e) {
            System.err.println("IOException for URL: " + url + " -> " + e.getMessage());
            throw e;
        }
    }
    public boolean isAllowedDomain(String url, Set<String> allowedDomains) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    public boolean isCrawlable(String link, Set<String> visitedUrls, Set<String> allowedDomains) {
        return (!link.isEmpty() && !visitedUrls.contains(link) && isValidLink(link)&&isAllowedDomain(link, allowedDomains));
    }

    public boolean isValidLink(String url) {
        try {
            if (!url.startsWith("www.")&&!url.startsWith("http://") && !url.startsWith("https://") && !url.endsWith("jar")) {
                return false; //Return false for non http/s like mailto: or ftp:
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
