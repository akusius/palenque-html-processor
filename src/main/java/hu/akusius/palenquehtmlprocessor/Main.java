package hu.akusius.palenquehtmlprocessor;

import hu.akusius.palenquehtmlprocessor.config.PageConfig;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import hu.akusius.palenquehtmlprocessor.config.SessionConfig;
import hu.akusius.palenquehtmlprocessor.config.SiteConfig;
import hu.akusius.palenquehtmlprocessor.doc.*;
import hu.akusius.palenquehtmlprocessor.html.EntityReplacer;
import hu.akusius.palenquehtmlprocessor.html.Formatter;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Bujdosó Ákos
 */
public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  private static final String UTF8 = "UTF-8";

  private static final DocProcessor[] docProcessors = new DocProcessor[]{
    new Cleaner(),
    new Identity(),
    new MetaInformation(),
    new Analytics(),
    new Anchors(),
    new Images(),
    new Disqus(),
    new ShareWidgets(),
    new RepoLink(),
    new Links(),
    new Toc(),
    new Arranger()
  };

  private static final HtmlProcessor[] htmlProcessors = new HtmlProcessor[]{
    new EntityReplacer(),
    new Formatter()
  };

  private static final List<Processor<?>> processors;

  static {
    List<Processor<?>> prs = new ArrayList<>(docProcessors.length + htmlProcessors.length);
    prs.addAll(Arrays.asList(docProcessors));
    prs.addAll(Arrays.asList(htmlProcessors));
    processors = Collections.unmodifiableList(prs);
  }

  /**
   * @param args the command line arguments
   * @throws java.lang.Exception
   */
  public static void main(String[] args) throws Exception {
    ArgumentParser ap = ArgumentParsers.newArgumentParser("PalenqueHtmlProcessor");
    SessionConfig.configParser(ap, processors);

    final Namespace ns;
    try {
      ns = ap.parseArgs(args);
    } catch (ArgumentParserException e) {
      ap.handleError(e);
      System.exit(1);
      return;
    }

    SessionConfig sessionConfig = SessionConfig.parseArguments(ns, processors);
    SiteConfig siteConfig = new SiteConfig(sessionConfig);

    logger.log(Level.INFO, "Running processors: {0}", String.join(", ",
            processors.stream()
            .filter(p -> p.conditionalRun() && !p.isDisabled())
            .map(p -> p.getParamName())
            .collect(Collectors.toList())));

    final Path destDir = sessionConfig.getDestDir();

    for (Processor<?> processor : processors) {
      if (!processor.isDisabled()) {
        processor.sessionStart(sessionConfig);
      }
    }

    List<Path> htmlFiles = new ArrayList<>(50);
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(sessionConfig.getSourceDir(), "*.html")) {
      for (Path path : ds) {
        htmlFiles.add(path);
      }
    }

    boolean preprocessNeeded = false;
    for (DocProcessor p : docProcessors) {
      if (!p.isDisabled() && p.isPreprocessNeeded()) {
        preprocessNeeded = true;
        break;
      }
    }

    if (preprocessNeeded) {
      for (Path src : htmlFiles) {
        try {
          String fileName = src.getFileName().toString();
          logger.log(Level.FINE, "Preprocessing {0}...", fileName);

          ProcessConfig processConfig = new ProcessConfig(new PageConfig(siteConfig, fileName));

          Document doc = Jsoup.parse(src.toFile(), UTF8);

          for (DocProcessor p : docProcessors) {
            if (!p.isDisabled() && p.isPreprocessNeeded()) {
              p.preprocess(doc.clone(), processConfig);
            }
          }
        } catch (Exception ex) {
          logger.log(Level.SEVERE, "Error preprocessing file: " + src, ex);
        }
      }
    }

    for (Path src : htmlFiles) {
      try {
        String fileName = src.getFileName().toString();
        logger.log(Level.FINE, "Processing {0}...", fileName);

        ProcessConfig processConfig = new ProcessConfig(new PageConfig(siteConfig, fileName));

        Document doc = Jsoup.parse(src.toFile(), UTF8);

        for (DocProcessor p : docProcessors) {
          doc = runProcessor(p, doc, processConfig);
        }

        doc.outputSettings().prettyPrint(false);
        String html = doc.toString();

        for (HtmlProcessor p : htmlProcessors) {
          html = runProcessor(p, html, processConfig);
        }

        final Path dest = destDir != null ? destDir.resolve(src.getFileName()) : src;

        if (dest == src && !siteConfig.isNoBackup()) {
          Files.copy(src, Paths.get(src + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        }

        Files.write(dest, html.getBytes(UTF8));
      } catch (Exception ex) {
        logger.log(Level.SEVERE, "Error processing file: " + src, ex);
      }
    }

    for (Processor<?> processor : processors) {
      if (!processor.isDisabled()) {
        processor.sessionEnd(sessionConfig);
      }
    }
  }

  private static <T> T runProcessor(Processor<T> processor, T object, ProcessConfig config) {
    try {
      if (!processor.isDisabled()) {
        return processor.process(object, config);
      }
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Error running processor (" + processor + ") on file: " + config.getPage(), ex);
    }
    return object;
  }
}
