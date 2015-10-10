package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import hu.akusius.palenquehtmlprocessor.config.SessionConfig;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;

/**
 *
 * @author Bujdosó Ákos
 */
public class Sitemap extends DocProcessor {

  private static final Logger logger = Logger.getLogger(Sitemap.class.getName());

  private List<String> pageUrls;

  @Override
  public void sessionStart(SessionConfig config) {
    super.sessionStart(config);
    this.pageUrls = new ArrayList<>(50);
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    pageUrls.add(config.getPageUrl());
  }

  @Override
  public void sessionEnd(SessionConfig config) {
    super.sessionEnd(config);
    pageUrls.sort(null);

    logger.log(Level.FINE, "Writing sitemap.txt...");

    Path dir = config.getDestDir();
    if (dir == null) {
      dir = config.getSourceDir();
    }

    try (BufferedWriter wr = Files.newBufferedWriter(dir.resolve("sitemap.txt"))) {
      for (String pageUrl : pageUrls) {
        wr.write(pageUrl);
        wr.write('\n');
      }
      wr.flush();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
