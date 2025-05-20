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
        String indent = "-->".repeat(node.depth);

        out.append("<br>").append(indent).append("link to: ").append(node.rawHtml).append("\n");
        out.append("<br>").append(indent).append("depth: ").append(node.depth).append("\n");

        for (String heading : node.headings) {
            out.append("# ").append(indent).append(heading.trim()).append("\n");
        }


        for (String linkLog : node.loggedLinks) {
            boolean uniqueLink = true;
            for (CrawlNode child : node.children) {
                if (("<br>" + indent + "link to " + child.rawHtml).equals(linkLog)) {
                    uniqueLink = false;
                    renderTree(child, out);
                }
            }
            if (uniqueLink) {
                out.append("<br>").append(indent).append(linkLog.trim()).append("\n");
            }

        }


        out.append("<br>\n");
    }


}

