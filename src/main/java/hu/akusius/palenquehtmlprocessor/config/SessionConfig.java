package hu.akusius.palenquehtmlprocessor.config;

import hu.akusius.palenquehtmlprocessor.Processor;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentChoice;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Bujdosó Ákos
 */
public class SessionConfig extends Config {

  private final boolean website;

  private final boolean secure;

  private final String domain;

  private final String path;

  private final String imagePath;

  private final Path sourceDir;

  private final Path destDir;

  private final boolean noBackup;

  private final String twitterHashtag;

  private final String twitterVia;

  public final boolean isWebsite() {
    return website;
  }

  public final boolean isSecure() {
    return secure;
  }

  public final String getDomain() {
    return domain;
  }

  public final String getPath() {
    return path;
  }

  public final String getImagePath() {
    return imagePath;
  }

  public final Path getSourceDir() {
    return sourceDir;
  }

  public final Path getDestDir() {
    return destDir;
  }

  public final boolean isNoBackup() {
    return noBackup;
  }

  public final String getAuthorId() {
    return DefaultAuthorId;
  }

  public final String getTwitterHashtag() {
    return twitterHashtag;
  }

  public final String getTwitterVia() {
    return twitterVia;
  }

  private SessionConfig(boolean website, boolean secure, String domain, String path, String imagePath,
          Path sourceDir, Path destDir, boolean noBackup, String twitterHashtag, String twitterVia) {
    this.website = website;
    this.secure = secure;
    this.domain = domain;
    this.path = path;
    this.imagePath = imagePath;
    this.sourceDir = sourceDir;
    this.destDir = destDir;
    this.noBackup = noBackup;
    this.twitterHashtag = twitterHashtag;
    this.twitterVia = twitterVia;
  }

  protected SessionConfig(SessionConfig config) {
    this(config.website, config.secure, config.domain, config.path, config.imagePath,
            config.sourceDir, config.destDir, config.noBackup, config.twitterHashtag, config.twitterVia);
  }

  public static void configParser(ArgumentParser ap, Iterable<Processor<?>> processors) {
    ap.addArgument("--src").setDefault(".").metavar("DIR").help("Source directory");
    ap.addArgument("--dest").metavar("DIR").help("Destination directory");
    ap.addArgument("--no-backup").type(Boolean.class).action(Arguments.storeTrue()).help("Do not backup HTML files before modifying");
    ap.addArgument("--website").metavar("T/F").type(Boolean.class).setDefault(true).help("Handle as a website (do not use index.html)");
    ap.addArgument("--secure").type(Boolean.class).action(Arguments.storeTrue()).help("Use HTTPS");
    ap.addArgument("--domain").setDefault(DefaultDomain).help("The domain of the document");
    ap.addArgument("--path").setDefault(DefaultPath).help("The path of the document root within the domain");
    ap.addArgument("--image").metavar("PATH").setDefault(DefaultImagePath).help("Path of the social image");
    ap.addArgument("--tw-hashtag").metavar("TAG(S)").setDefault(DefaultTwitterHashtag).help("Twitter hashtag(s)");
    ap.addArgument("--tw-via").metavar("VIA").setDefault(DefaultTwitterVia).help("Twitter via");
    ap.addArgument("-r", "--reverse-mode").type(Boolean.class).action(Arguments.storeTrue()).help("Reverse mode (disable the specified processors)");

    ProcessorChoice processorChoice = new ProcessorChoice(processors);
    if (!processorChoice.isEmpty()) {
      ap.addArgument("processors").choices(processorChoice).nargs("*")
              .help("The processors to run (or to disable in reverse mode)");
    }
    ap.defaultHelp(true);
  }

  public static SessionConfig parseArguments(Namespace ns, Collection<Processor<?>> processors) {
    boolean website = ns.getBoolean("website");
    boolean secure = ns.getBoolean("secure");

    String domain = ns.getString("domain").trim();
    if (domain.contains("/")) {
      throw new IllegalArgumentException("Invalid domain: " + domain);
    }

    String path = normalizePath(ns.getString("path"));
    String imagePath = normalizePath(ns.getString("image"));

    final Path sourceDir = Paths.get(ns.getString("src"));
    checkPQHDirectory(sourceDir);

    final Path destDir;
    String destDirS = ns.getString("dest");
    if (destDirS == null || destDirS.length() == 0) {
      destDir = null;
    } else {
      destDir = Paths.get(destDirS);
      File ddf = destDir.toFile();
      if (!ddf.exists() || !ddf.isDirectory()) {
        throw new IllegalArgumentException("Invalid destination directory: " + destDir);
      }
    }

    boolean noBackup = ns.getBoolean("no_backup");

    String twitterHashtag = ns.getString("tw_hashtag");
    String twitterVia = ns.getString("tw_via");

    Set<Processor<?>> specifiedProcessors = ProcessorChoice.specified(ns.<String>getList("processors"), processors);
    if (!specifiedProcessors.isEmpty()) {
      // Disable processors (in direct mode if not specified, in reverse mode if specified)
      boolean reverseMode = ns.getBoolean("reverse_mode");
      for (Processor<?> processor : processors) {
        if (processor.conditionalRun()) {
          boolean specified = specifiedProcessors.contains(processor);
          if ((!reverseMode && !specified) || (reverseMode && specified)) {
            processor.disable();
          }
        }
      }
    }

    return new SessionConfig(website, secure, domain, path, imagePath, sourceDir, destDir, noBackup, twitterHashtag, twitterVia);
  }

  private static String normalizePath(String path) {
    path = path.trim();
    while (path.startsWith("/")) {
      path = path.substring(1);
    }
    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 2);
    }
    return path;
  }

  private static void checkPQHDirectory(Path dir) {
    File f = dir.toFile();
    if (!f.exists() || !f.isDirectory()) {
      throw new IllegalArgumentException("Invalid source directory: " + dir);
    }

    Path index = dir.resolve("index.html");
    f = index.toFile();
    if (!f.exists()) {
      throw new IllegalArgumentException("Missing index.html in directory: " + dir);
    }

    final Document doc;
    try {
      doc = Jsoup.parse(f, "UTF-8");
    } catch (Exception ex) {
      throw new IllegalArgumentException("index.html not readable in directory: " + dir, ex);
    }
    if (!doc.title().toLowerCase().contains("palenque code")) {
      throw new IllegalArgumentException("index.html is not a 'Palenque Code' document in directory: " + dir);
    }
  }

  private static class ProcessorChoice implements ArgumentChoice {

    private final List<String> names = new ArrayList<>(20);

    private final String textualFormat;

    ProcessorChoice(Iterable<Processor<?>> processors) {
      for (Processor<?> processor : processors) {
        if (processor.conditionalRun()) {
          names.add(processor.getParamName());
        }
      }
      textualFormat = "{" + String.join(",", names) + "}";
    }

    public boolean isEmpty() {
      return names.isEmpty();
    }

    @Override
    public boolean contains(Object val) {
      if (val instanceof String) {
        String param = (String) val;
        int match = 0;
        for (String name : names) {
          if (match(name, param)) {
            match++;
          }
        }
        return match == 1;
      }
      return false;
    }

    public static Set<Processor<?>> specified(List<String> params, Collection<Processor<?>> processors) {
      Set<Processor<?>> result = new HashSet<>(processors.size());
      for (Processor<?> processor : processors) {
        for (String param : params) {
          if (match(processor.getParamName(), param)) {
            result.add(processor);
          }
        }
      }
      return result;
    }

    private static boolean match(String name, String param) {
      return name.toLowerCase().startsWith(param.toLowerCase());
    }

    @Override
    public String textualFormat() {
      return textualFormat;
    }

    @Override
    public String toString() {
      return textualFormat;
    }
  }
}
