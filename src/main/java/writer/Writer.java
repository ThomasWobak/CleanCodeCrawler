package writer;


import crawler.CrawlNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Writer {
    private final String filePath;

    public Writer(String filePath) {
        this.filePath = filePath;
    }

    public void saveToMarkdown(List<CrawlNode> roots) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (CrawlNode root : roots) {
            renderTree(root, sb);
        }

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, sb.toString().getBytes());
        System.out.println("Saved to Markdown");
    }

    private void renderTree(CrawlNode node, StringBuilder out) {
        if (node.depth > node.maxDepth) {
            for (String linkLog : node.loggedLinks) {
                out.append("-->".repeat(node.depth)).append(linkLog).append("\n");
            }
        } else {
            out.append("-->".repeat(node.depth)).append("\n<br>link to: ").append(node.rawHtml).append("\n");
            out.append("-->".repeat(node.depth)).append("<br>depth: ").append(node.depth).append("\n");
            for (String h : node.headings) {
                out.append(h);
            }
            for (CrawlNode child : node.children) {
                renderTree(child, out);
            }
            out.append("\n");
        }
    }
}

