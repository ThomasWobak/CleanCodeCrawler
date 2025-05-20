package parser;

import java.util.Set;

public class ParsedInputArguments {
    private String startUrl;
    private int maxDepth;
    private Set<String> allowedDomains;



    public String getStartUrl() {
        return startUrl;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Set<String> getAllowedDomains() {
        return allowedDomains;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setAllowedDomains(Set<String> allowedDomains) {
        this.allowedDomains = allowedDomains;
    }
}
