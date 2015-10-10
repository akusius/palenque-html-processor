package hu.akusius.palenquehtmlprocessor.doc;

import hu.akusius.palenquehtmlprocessor.DocProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Bujdosó Ákos
 */
public class Anchors extends DocProcessor {

  private static final Logger logger = Logger.getLogger(Anchors.class.getName());

  @Override
  protected void processDoc(Document doc, ProcessConfig config) {
    if (config.isFrontPage()) {
      return;
    }

    Set<String> idSet = doc.getAllElements().stream()
            .filter(e -> !e.id().isEmpty())
            .map(e -> e.id())
            .collect(Collectors.toSet());

    for (Element p : doc.getElementsByTag("p")) {
      if (p.id().isEmpty()) {
        final String id = generateId(idSet);
        p.attr("id", id);
        logger.log(Level.FINER, "Adding ''id'' to paragraph: {0}", id);
      }

      if (!hasElement(p, (r, e) -> "a".equals(e.tagName()) && e.hasClass("anchor"))) {
        final Element a = p.prependElement("a");
        a.addClass("anchor");
        a.attr("href", "#" + p.id());

        String text = p.text();
        if (text.length() > 50) {
          text = text.substring(0, 50) + '…';
        }
        logger.log(Level.FINE, "Adding anchor ({0}) to paragraph: {1}", new Object[]{p.id(), text});
      }
    }
  }

  private static String generateId(Set<String> idSet) {
    return generateId(idSet, true);
  }

  private static String generateId(Set<String> idSet, boolean putIntoSet) {
    while (true) {
      String id = generateId();
      if (idSet.contains(id)) {
        continue;
      }
      if (putIntoSet) {
        idSet.add(id);
      }
      return id;
    }
  }

  private static final Random idRandom = new Random();

  private static final char[] idCharSet = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  private static String generateId() {
    StringBuilder sb = new StringBuilder(5);
    sb.append('a');
    sb.append(idCharSet[idRandom.nextInt(idCharSet.length)]);
    sb.append(idCharSet[idRandom.nextInt(idCharSet.length)]);
    sb.append(idCharSet[idRandom.nextInt(idCharSet.length)]);
    return sb.toString();
  }
}
