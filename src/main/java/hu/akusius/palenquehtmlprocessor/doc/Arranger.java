package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public class Arranger extends DocProcessor {

  @Override
  public boolean conditionalRun() {
    return false;
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    // Relocate <style> element to the end of the head
    Element style = doc.head().getElementsByTag("style").first();
    if (style != null) {
      if (style.nextElementSibling() != null) {
        Element parent = style.parent();
        removeWithNewline(style);
        parent.appendChild(style);
        parent.appendText("\n");
      }
    }
  }
}
