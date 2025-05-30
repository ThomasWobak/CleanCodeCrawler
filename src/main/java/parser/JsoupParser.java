package parser;

import dto.HeadingInfo;
import dto.LinkInfo;
import dto.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

public class JsoupParser implements Parser {
    private static final int TIMEOUT = 20_000;


    @Override
    public Page parsePage(String url) throws IOException {
        Document doc = fetchDocument(url);
        List<HeadingInfo> headings = extractHeadingsFromDocument(doc);
        List<LinkInfo> links = extractLinksFromDocument(doc);
        return new Page(url, headings, links);
    }

    protected Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; GileadCrawler/1.0)")
                .timeout(TIMEOUT)
                .followRedirects(true)
                .get();
    }

    protected List<HeadingInfo> extractHeadingsFromDocument(Document doc) {
        return doc.select("h1, h2, h3")
                .stream()
                .map(e -> new HeadingInfo(
                        Integer.parseInt(e.tagName().substring(1)),
                        e.text()))
                .toList();
    }

    protected List<LinkInfo> extractLinksFromDocument(Document doc) {
        return doc.select("a[href]")
                .stream()
                .map(e -> new LinkInfo(e.outerHtml(), e.absUrl("href").trim()))
                .toList();
    }
}
