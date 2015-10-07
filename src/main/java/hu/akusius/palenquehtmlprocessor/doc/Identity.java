package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public final class Identity extends DocProcessor {

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    Element head = doc.head();
    getOrCreateTagAttr(head, "link", "rel", "canonical").attr("href", config.getPageUrl());
    if (config.getAuthorId() != null) {
      getOrCreateTagAttr(head, "link", "rel", "author").attr("href", config.getAuthorId());
    }
  }
}
