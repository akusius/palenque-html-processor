package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public final class MetaInformation extends DocProcessor {

  private static final Logger logger = Logger.getLogger(MetaInformation.class.getName());

  @Override
  public String getParamName() {
    return "Meta";
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    String title = doc.title();
    if (title.isEmpty()) {
      logger.log(Level.WARNING, "Missing title: {0}", config.getPage());
    }

    String description = getDescription(doc).trim();
    if (description == null || description.isEmpty()) {
      logger.log(Level.WARNING, "Missing description: {0}", config.getPage());
      description = "";
    }

    String url = config.getPageUrl();
    String image = config.getImageUrl();

    Element head = doc.head();

    // Open Graph
    head.attr("prefix", "og: http://ogp.me/ns#");   // simple approach, as we do not use other prefixes (hopefully)
    addMetaProperty(head, "og:locale", "en_US");
    addMetaProperty(head, "og:type", config.isFrontPage() && config.isWebsite() ? "website" : "article");
    addMetaProperty(head, "og:url", url);
    addMetaProperty(head, "og:title", title);
    addMetaProperty(head, "og:description", description);
    addMetaProperty(head, "og:image", image);

    // Twitter
    addMetaName(head, "twitter:card", "summary");
    addMetaName(head, "twitter:url", url);
    addMetaName(head, "twitter:title", title);
    addMetaName(head, "twitter:description", description);
    addMetaName(head, "twitter:image", image);
  }

  private static void addMetaProperty(Element head, String property, String content) {
    addMeta(head, "property", property, content);
  }

  private static void addMetaName(Element head, String name, String content) {
    addMeta(head, "name", name, content);
  }

  private static void addMeta(Element head, String key, String value, String content) {
    getOrCreateTagAttr(head, "meta", key, value).attr("content", content);
  }
}
