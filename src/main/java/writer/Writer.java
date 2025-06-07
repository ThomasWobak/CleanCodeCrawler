package writer;

import crawler.CrawlNode;
import exception.writer.WriterException;
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

    public static Writer create(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or empty");
        }
        return new Writer(filePath);
    }

    public void saveToMarkdown(List<CrawlNode> roots) {
        StringBuilder sb = new StringBuilder();
        for (CrawlNode root : roots) {
            renderTree(root, sb);
        }

        Path path = Paths.get(filePath);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, sb.toString().getBytes());
            System.out.println("Saved to Markdown");
        } catch (IOException e) {
            throw new WriterException("Could not write report to " + path, e);
        }
    }

    private void renderTree(CrawlNode node, StringBuilder out) {
        String indent = "-->".repeat(node.depth);

        out.append("<br>").append(indent)
                .append("link to: ").append(node.rawHtml).append("\n");
        out.append("<br>").append(indent)
                .append("depth: ").append(node.depth).append("\n");

        for (String heading : node.headings) {
            out.append("<br>").append(indent)
                    .append("# ").append(heading.trim()).append("\n");
        }
        for (String linkLog : node.loggedLinks) {
            out.append("<br>").append(indent)
                    .append(linkLog.trim()).append("\n");
        }

        for (CrawlNode child : node.children) {
            renderTree(child, out);
        }
        out.append("<br>\n\n");
    }
}
