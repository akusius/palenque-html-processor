package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public class Analytics extends DocProcessor {

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    final Element head = doc.head();

    // Remove old scripts
    for (Element script : head.getElementsByTag("script")) {
      if (script.attr("src").contains("analytics.js")) {
        removeWithNewline(script);
      }
    }

    // Add new
    Element script = head.appendElement("script");
    script.attr("type", "text/javascript");
    script.attr("src", "js/analytics.js");
    head.appendText("\n");
  }
}
