package service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Set;

public class UrlService {
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 20000;

    public String normalize(String rawUrl) {
        try {
            URI u = new URI(rawUrl.trim())
                    .normalize();  // collapses “../” etc.
            String path = u.getPath().replaceAll("/+$", ""); // strip trailing slash
            return new URI(
                    u.getScheme().toLowerCase(),
                    u.getAuthority().toLowerCase(),
                    path,
                    null, null
            ).toString();
        } catch (Exception e) {
            return rawUrl.trim();
        }
    }

    public boolean isValid(String url) {
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

    public boolean isAllowedDomain(String url, Set<String> allowedDomains) {
        return allowedDomains.stream().anyMatch(url::contains);
    }

    public boolean isCrawlable(String url, Set<String> visited, Set<String> domains) {
        return url != null
                && !url.isEmpty()
                && !visited.contains(url)
                && isValid(url)
                && isAllowedDomain(url, domains);
    }
}
