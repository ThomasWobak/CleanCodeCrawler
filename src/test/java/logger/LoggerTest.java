package logger;

import crawler.CrawlNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    private Logger logger;

    @BeforeEach
    protected void setUp() {
        logger = new Logger();
    }

    @Test
    protected void testLogHeadings_singleLevel0() {
        CrawlNode node = new CrawlNode("http://example.com", "", 0, 1);
        Document doc = Jsoup.parse("<h1>Main Title</h1>");
        logger.logHeadings(node, doc);

        assertEquals(1, node.headings.size());
        assertEquals("# Main Title\n", node.headings.get(0));
    }

    @Test
    protected void testLogHeadings_multipleLevelsDepth2() {
        CrawlNode node = new CrawlNode("http://example.com", "", 2, 1);
        String html = "<h2>Subheading</h2><h3>Sub-sub</h3>";
        Document doc = Jsoup.parse(html);
        logger.logHeadings(node, doc);

        assertEquals(2, node.headings.size());
        assertEquals("-->--># # Subheading\n", node.headings.get(0));
        assertEquals("-->--># # # Sub-sub\n", node.headings.get(1));
    }

    @Test
    protected void testLogHeadings_variousLevelsDepth1() {
        CrawlNode node = new CrawlNode("http://example.com", "", 1, 1);
        String html = "<h1>H1</h1><h2>H2</h2><h3>H3</h3><h4>H4</h4><h5>H5</h5><h6>H6</h6>";
        Document doc = Jsoup.parse(html);
        logger.logHeadings(node, doc);

        assertEquals(6, node.headings.size());
        assertEquals("--># H1\n", node.headings.get(0));
        assertEquals("--># # H2\n", node.headings.get(1));
        assertEquals("--># # # H3\n", node.headings.get(2));
        assertEquals("--># # # # H4\n", node.headings.get(3));
        assertEquals("--># # # # # H5\n", node.headings.get(4));
        assertEquals("--># # # # # # H6\n", node.headings.get(5));
    }

    @Test
    protected void testLogHeadings_deeperDepthLevel4() {
        CrawlNode node = new CrawlNode("http://example.com", "", 4, 1);
        Document doc = Jsoup.parse("<h4>Deep H4</h4>");
        logger.logHeadings(node, doc);

        assertEquals(1, node.headings.size());
        assertEquals("-->-->-->--># # # # Deep H4\n", node.headings.get(0));
    }

    @Test
    protected void testLogHeadings_noHeadings() {
        CrawlNode node = new CrawlNode("http://example.com", "", 0, 1);
        Document doc = Jsoup.parse("<p>No headings here</p>");
        logger.logHeadings(node, doc);

        assertTrue(node.headings.isEmpty());
    }

    @Test
    protected void testLogGetHeadingLevel_invalidTag() {
        CrawlNode node = new CrawlNode("http://example.com", "", 0, 1);
        Document doc = Jsoup.parse("<hX>Oops</hX>");
        logger.logHeadings(node, doc);

        assertTrue(node.headings.isEmpty());
    }

    @Test
    protected void testLogBrokenLink_depth1() {
        CrawlNode node = new CrawlNode("http://example.com", "", 1, 1);
        String rawHtml = "<a href=\"/foo\">Foo</a>";
        logger.logBrokenLink(node, rawHtml);

        assertEquals(1, node.loggedLinks.size());
        assertEquals("<br>-->broken link " + rawHtml, node.loggedLinks.get(0));
    }

    @Test
    protected void testLogLink_depth3() {
        CrawlNode node = new CrawlNode("http://example.com", "", 3, 1);
        String rawHtml = "<a href=\"/bar\">Bar</a>";
        logger.logLink(node, rawHtml);

        assertEquals(1, node.loggedLinks.size());
        assertEquals("<br>-->-->-->link to " + rawHtml, node.loggedLinks.get(0));
    }
    @Test
    protected void testGetHeadingLevel_emptyTagNameFallsBackToOne() throws Exception {

        java.lang.reflect.Method m = Logger.class.getDeclaredMethod("getHeadingLevel", String.class);
        m.setAccessible(true);

        int level = (int) m.invoke(logger, "");
        assertEquals(1, level, "Empty or malformed tag names should fall back to level 1");
    }

}
