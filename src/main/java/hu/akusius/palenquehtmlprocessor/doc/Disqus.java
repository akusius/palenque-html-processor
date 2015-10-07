package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public class Disqus extends DocProcessor {

  private static final String fragment
          = "\n<div id=\"disqus_thread\"></div>\n"
          + "<script type=\"text/javascript\" src=\"js/disqus.js\"></script>\n"
          + "<noscript>Please enable JavaScript to view the "
          + "<a href=\"https://disqus.com/?ref_noscript\" rel=\"nofollow\">"
          + "comments powered by Disqus.</a></noscript>\n";

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    Element body = doc.body();
    Element comments = getOrCreateTagAttr(body, "div", "id", "comments");
    comments.empty();

    comments.append(fragment);
  }
}
