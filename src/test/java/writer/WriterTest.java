package writer;

import crawler.CrawlNode;
import exception.writer.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

 class WriterTest {
    @TempDir
    Path tempDir;

    private Path filePath;
    private Writer writer;

    @BeforeEach
    protected void setUp() {
        filePath = tempDir.resolve("output/reports.md");
        writer = new Writer(filePath.toString());
    }

    @Test
    protected void testSaveToMarkdown_emptyRoots_createsEmptyFile() throws IOException {
        writer.saveToMarkdown(Collections.emptyList());
        assertTrue(Files.exists(filePath));
        String content = Files.readString(filePath);
        assertEquals("", content, "Empty roots should produce empty file");
    }

    @Test
    protected void testSaveToMarkdown_singleNode() throws IOException {
        CrawlNode root = new CrawlNode("http://example.com", "<a>http://example.com</a>", 0, 1);
        root.headings.add("Title");
        root.loggedLinks.add("<a href=\"/foo\">Foo</a>");

        writer.saveToMarkdown(List.of(root));
        assertTrue(Files.exists(filePath));
        String content = Files.readString(filePath);

        String expected = """
                <br>link to: <a>http://example.com</a>
                <br>depth: 0
                <br># Title
                <br><a href="/foo">Foo</a>
                <br>

                """;

        assertEquals(expected, content);
    }

    @Test
    protected void testSaveToMarkdown_nestedChildren() throws IOException {
        // root node
        CrawlNode root = new CrawlNode("http://root", "<a>root</a>", 0, 2);
        root.headings.add("RootHeading");
        root.loggedLinks.add("<a href=\"/child1\">Child1Link</a>");

        // child node
        CrawlNode child = new CrawlNode("http://root/child", "<a>child</a>", 1, 2);
        child.headings.add("ChildHeading");
        child.loggedLinks.add("<a href=\"/subchild\">SubChildLink</a>");

        root.children.add(child);

        writer.saveToMarkdown(List.of(root));
        String content = Files.readString(filePath);

        String expected = """
                <br>link to: <a>root</a>
                <br>depth: 0
                <br># RootHeading
                <br><a href="/child1">Child1Link</a>
                <br>-->link to: <a>child</a>
                <br>-->depth: 1
                <br>--># ChildHeading
                <br>--><a href="/subchild">SubChildLink</a>
                <br>

                <br>
                
                """;

        assertEquals(expected, content);
    }

    @Test
    protected void testSaveToMarkdown_multipleRoots() throws IOException {
        CrawlNode node1 = new CrawlNode("http://a", "<a>a</a>", 0, 1);
        node1.headings.add("A");
        CrawlNode node2 = new CrawlNode("http://b", "<a>b</a>", 0, 1);
        node2.headings.add("B");

        writer.saveToMarkdown(List.of(node1, node2));
        String content = Files.readString(filePath);
        assertTrue(content.contains("<br>link to: <a>a</a>"));
        assertTrue(content.contains("<br># A\n"));
        assertTrue(content.contains("<br>link to: <a>b</a>"));
        assertTrue(content.contains("<br># B\n"));
    }
    @Test
    protected void testSaveToMarkdown_minimalNode() throws IOException {
        // A root node with no headings, no loggedLinks, no children
        CrawlNode node = new CrawlNode("http://example.com", "<a>example</a>", 0, 1);
        writer.saveToMarkdown(List.of(node));
        String content = Files.readString(filePath);
        String expected = """
                <br>link to: <a>example</a>
                <br>depth: 0
                <br>
                
                """;
        assertEquals(expected, content);
    }

    @Test
    protected void testSaveToMarkdown_invalidParentDirectory_throwsIOException() {
        Path parentAsFile = tempDir.resolve("notADir");
        assertDoesNotThrow(() -> Files.createFile(parentAsFile));
        Path badFilePath = parentAsFile.resolve("out.md");
        Writer badWriter = new Writer(badFilePath.toString());
        assertThrows(WriterException.class, () -> badWriter.saveToMarkdown(Collections.emptyList()));
    }
}
