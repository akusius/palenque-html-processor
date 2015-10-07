package hu.akusius.palenquehtmlprocessor;

import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.jsoup.select.QueryParserProxy;

/**
 *
 * @author Bujdosó Ákos
 */
public abstract class DocProcessor extends Processor<Document> {

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

  protected static Elements getElements(Element elem, Evaluator evaluator) {
    return Collector.collect(new org.jsoup.select.Evaluator() {

      @Override
      public boolean matches(Element root, Element element) {
        return evaluator.matches(root, element);
      }
    }, elem);
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

  @FunctionalInterface
  @SuppressWarnings("PublicInnerClass")
  public interface Evaluator {

    boolean matches(Element root, Element element);
  }
}
