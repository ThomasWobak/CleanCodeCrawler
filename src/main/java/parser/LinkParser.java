package parser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LinkParser {
    private static final int INVALIDRESPONSECODES = 400;
    private static final int TIMEOUTMILLISECONDS = 20000;

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
