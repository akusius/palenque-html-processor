package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import hu.akusius.palenquehtmlprocessor.config.SessionConfig;
import hu.akusius.palenquehtmlprocessor.config.SiteConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.QueryParserProxy;

/**
 *
 * @author Bujdosó Ákos
 */
public class Links extends DocProcessor {

  private static final Logger logger = Logger.getLogger(Links.class.getName());

  private static final Pattern headingPattern = Pattern.compile("h[1-6]");

  private static final org.jsoup.select.Evaluator scLink
          = QueryParserProxy.parse("div#share a, div#comments a");

  private Map<String, String> destTitles;

  @Override
  public void sessionStart(SessionConfig config) {
    super.sessionStart(config);
    destTitles = new HashMap<>(100);
  }

  @Override
  public boolean isPreprocessNeeded() {
    return true;
  }

  @Override
  public void preprocess(Document doc, ProcessConfig config) {
    final String page = config.getPage();

    String title = doc.title();
    if (title.isEmpty()) {
      logger.log(Level.WARNING, "Missing title: {0}", page);
      return;
    }

    int i = title.lastIndexOf('|');
    if (i >= 0) {
      title = title.substring(0, i).trim();
    }
    if (title.isEmpty()) {
      logger.log(Level.WARNING, "Invalid title format: {0}", page);
      return;
    }

    putDestTitle(page, title);

    for (Element heading : getElements(doc, (r, e) -> !e.id().isEmpty()
            && headingPattern.matcher(e.tagName()).matches())) {
      putDestTitle(page + '#' + heading.id(), heading.text() + " | " + title);
    }

    for (Element p : getElements(doc, (r, e) -> !e.id().isEmpty() && "p".equals(e.tagName()))) {
      Element heading = searchHeading(p);
      putDestTitle(page + '#' + p.id(), heading != null ? heading.text() + " | " + title : title);
    }
  }

  private void putDestTitle(String dest, String title) {
    destTitles.put(dest, title);
    logger.log(Level.FINEST, "Adding destination title: {0}: {1}", new Object[]{dest, title});
  }

  private static Element searchHeading(Element e) {
    while ((e = prevElem(e)) != null) {
      if ("div".equals(e.tagName()) && "content".equals(e.id())) {
        break;
      }
      if (headingPattern.matcher(e.tagName()).matches()) {
        return e;
      }
    }
    return null;
  }

  private static Element prevElem(Element e) {
    Element pe = e.previousElementSibling();
    if (pe != null) {
      return pe;
    }
    return e.parent();
  }

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    for (Element a : doc.getElementsByTag("a")) {
      if (a.hasAttr("href")) {
        final String href = a.attr("href");

        if (href.startsWith("media/")) {
          // Media link
          continue;
        }

        if (scLink.matches(doc, a)) {
          // Share && comments link
          continue;
        }

        // Checking external class
        final boolean external = href.contains(":");
        final boolean mailto = href.startsWith("mailto:");
        if (a.hasClass("external") != external) {
          if (external && !mailto) {
            logger.log(Level.WARNING, "No ''external'' class on external link: {0}", href);
          }
          if (!external) {
            logger.log(Level.WARNING, "Invalid ''external'' class on internal link: {0}", href);
          }
        }
        if (external) {
          logger.log(Level.FINEST, "Skipping external link: {0}", href);
          continue;
        }

        // Split off fragment part
        String path = href;
        String fragment = null;
        if (path.contains("#")) {
          int i = path.indexOf('#');
          fragment = path.substring(i);
          path = path.substring(0, i);
        }

        final String page;
        if (SiteConfig.isFrontPage(path)) {
          // Handling front page links
          path = config.isWebsite() ? "./" : "index.html";
          if (fragment != null) {
            path += fragment;
          }
          a.attr("href", path);
          if (!href.equals(path)) {
            logger.log(Level.FINE, "Rewriting front page link: {0} -> {1}", new Object[]{href, path});
          }
          page = "index.html";
        } else {
          page = path.trim();
        }

        changeInnerLinkTitle(a, href, page, fragment);
      }
    }
  }

  private void changeInnerLinkTitle(Element a, String href, String page, String fragment) {
    if (page.isEmpty()) {
      return;
    }
    if ("h1".equals(a.parent().tagName())) {
      return;
    }

    final List<String> dests = new ArrayList<>(5);
    if (fragment != null) {
      dests.add(page + fragment);
    }
    dests.add(page);

    String newTitle = null;
    for (String dest : dests) {
      newTitle = destTitles.get(dest);
      if (newTitle == null) {
        logger.log(Level.WARNING, "Missing destination title: {0}", dest);
        continue;
      }
      break;
    }

    if (newTitle == null) {
      return;
    }

//    if (a.text().contains(newTitle)) {
//      newTitle = "";
//    }
    String currentTitle = a.attr("title");
    if (!currentTitle.equals(newTitle)) {
      logger.log(Level.INFO, "Rewriting link title: {0}: {1} -> {2}",
              new Object[]{href, currentTitle, newTitle});
    }

    if (newTitle.isEmpty()) {
      a.removeAttr("title");
      logger.log(Level.FINER, "Removing link title: {0}", href);
    } else {
      a.attr("title", newTitle);
      logger.log(Level.FINER, "Setting link title: {0}: {1}", new Object[]{href, newTitle});
    }
  }
}
