package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 *
 * @author Bujdosó Ákos
 */
public class Cleaner extends DocProcessor {

  @Override
  public boolean conditionalRun() {
    return false;
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    // Remove "\n" text nodes from the end of the body
    final Element body = doc.body();
    List<Node> childNodes = body.childNodes();
    for (int i = childNodes.size() - 1; i >= 0; i--) {
      Node node = childNodes.get(i);
      if (!(node instanceof TextNode)) {
        break;
      }
      TextNode tn = (TextNode) node;
      if (!"\n".equals(tn.getWholeText())) {
        break;
      }
      tn.remove();
    }
  }
}
