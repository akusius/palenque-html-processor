package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public class Images extends DocProcessor {

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    if (config.isFrontPage()) {
      return;
    }
    for (Element img : doc.getElementsByTag("img")) {
      if ("a".equals(img.parent().tagName()) || img.hasClass("icon") || img.hasClass("inline")) {
        continue;
      }

      String src = img.attr("src");
      if (src.contains(":")) {
        continue;
      }

      img.wrap("<a></a>");
      Element a = img.parent();
      if (!"a".equals(a.tagName())) {
        throw new IllegalStateException();
      }
      a.addClass("img");
      a.attr("href", src);
      a.prependText("\n");
      a.appendText("\n");
    }
  }
}
