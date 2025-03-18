package crawler;

public class Crawler {
    private final String startUrl;
    private final int depth;
    private final String domain;
    private int currentDepth;
    private String[] visitedUrls;

    public Crawler(String startUrl, int depth, String domain) {
        this.startUrl = startUrl;
        this.depth=depth;
        this.domain=domain;
    }
    public void crawl(){

    }
    private boolean checkUnvisitedAndValidUrl(String url){
        return false;
    }
    private void logUrl(){

    }
    private String[] getAllLinksFromSite(String url){
        return null;
    }

}
