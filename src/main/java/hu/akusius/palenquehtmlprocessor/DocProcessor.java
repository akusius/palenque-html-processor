package hu.akusius.palenquehtmlprocessor;

import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.*;

/**
 *
 * @author Bujdosó Ákos
 */
public abstract class DocProcessor extends Processor<Document> {

  private static final Logger logger = Logger.getLogger(DocProcessor.class.getName());

  @Override
  public Class<Document> getType() {
    return Document.class;
  }

  public boolean isPreprocessNeeded() {
    return false;
  }

  public void preprocess(Document doc, ProcessConfig config) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final Document process(Document doc, ProcessConfig config) {
    if (isDisabled()) {
      throw new IllegalStateException("Processor is disabled!");
    }
    processDoc(doc, config);
    return doc;
  }

  protected abstract void processDoc(Document doc, ProcessConfig config);

  protected static boolean hasElement(Element root, String query) {
    return hasElement(root, Evaluator.fromQuery(query));
  }

  protected static boolean hasElement(Element root, Evaluator evaluator) {
    return getElements(root, evaluator).size() > 0;
  }

  protected static Elements getElements(Element root, String query) {
    return getElements(root, Evaluator.fromQuery(query));
  }

  protected static Elements getElements(Element root, Evaluator evaluator) {
    return Collector.collect(new org.jsoup.select.Evaluator() {

      @Override
      public boolean matches(Element root, Element element) {
        return evaluator.matches(root, element);
      }
    }, root);
  }

  protected static Element getTagAttr(Element root, String tagName, String attrKey, String attrValue) {
    return getElements(root, (r, e) -> tagName.equals(e.tagName()) && attrValue.equals(e.attr(attrKey))).first();
  }

  protected static Element getOrCreateTagAttr(Element root, String tagName, String attrKey, String attrValue) {
    Element elem = getTagAttr(root, tagName, attrKey, attrValue);
    if (elem == null) {
      elem = root.appendElement(tagName);
      elem.attr(attrKey, attrValue);
      root.appendText("\n");
    }
    return elem;
  }

  protected static String getDescription(Document doc) {
    Element elem = getTagAttr(doc.head(), "meta", "name", "description");
    return elem != null ? elem.attr("content") : "";
  }

  protected static boolean matches(Element e, String cssQuery) {
    return QueryParserProxy.parse(cssQuery).matches(e.ownerDocument(), e);
  }

  protected static void removeWithNewline(Element e) {
    // First try to remove the newline after
    Node node = e.nextSibling();
    if (node instanceof TextNode) {
      TextNode tn = (TextNode) node;
      String text = tn.getWholeText();
      if (text.startsWith("\n")) {
        if (text.length() == 1) {
          tn.remove();
        } else {
          tn.text(text.substring(1));
        }
      }
    }
    e.remove();
  }

  protected static boolean hasScriptFile(Element root, String src) {
    return hasElement(root, (r, e) -> "script".equals(e.tagName()) && e.attr("src").equals(src));
  }

  protected static void addScriptFile(Element root, String src) {
    root.appendElement("script").attr("type", "text/javascript").attr("src", src);
    root.appendText("\n");
    logger.log(Level.FINER, "Add script file: {0}", src);
  }

  protected static void addScriptFileIfNeeded(Element root, String src) {
    if (!hasScriptFile(root, src)) {
      addScriptFile(root, src);
    }
  }

  protected final static String jquery = "js/lib/jquery.min.js";

  protected static void addJqueryScript(Element root) {
    addScriptFileIfNeeded(root, jquery);
  }

  @FunctionalInterface
  @SuppressWarnings("PublicInnerClass")
  public interface Evaluator {

    boolean matches(Element root, Element element);

    static Evaluator fromEvaluator(final org.jsoup.select.Evaluator evaluator) {
      return (r, e) -> evaluator.matches(r, e);
    }

    static Evaluator fromQuery(String query) {
      return fromEvaluator(QueryParserProxy.parse(query));
    }
  }
}
