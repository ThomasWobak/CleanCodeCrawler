package dto;

import java.util.List;

public class Page {
    private final String url;
    private final List<HeadingInfo> headings;
    private final List<LinkInfo> outgoingLinks;

    public Page(String url, List<HeadingInfo> headings, List<LinkInfo> outgoingLinks) {
        this.url = url;
        this.headings = headings;
        this.outgoingLinks = outgoingLinks;
    }

    public String getUrl() {
        return url;
    }

    public List<HeadingInfo> getHeadings() {
        return headings;
    }

    public List<LinkInfo> getOutgoingLinks() {
        return outgoingLinks;
    }
}
