package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;

/**
 *
 * @author Bujdosó Ákos
 */
public class Analytics extends DocProcessor {

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    addScriptFileIfNeeded(doc.head(), "js/analytics.js");
  }
}
