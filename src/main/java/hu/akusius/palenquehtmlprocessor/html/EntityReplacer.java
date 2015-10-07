package hu.akusius.palenquehtmlprocessor.html;

import hu.akusius.palenquehtmlprocessor.HtmlProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Bujdosó Ákos
 */
public final class EntityReplacer extends HtmlProcessor {

  private static final Map<Character, String> entities;

  static {
    entities = new HashMap<>(20);
    entities.put('←', "larr");
    entities.put('↑', "uarr");
    entities.put('→', "rarr");
    entities.put('↓', "darr");
    entities.put('−', "minus");
    entities.put('×', "times");
    entities.put('√', "radic");
    entities.put('≈', "asymp");
  }

  @Override
  public boolean conditionalRun() {
    return false;
  }

  @Override
  protected String processHtml(String html, ProcessConfig config) {
    StringBuilder sb = new StringBuilder(html.length() + 100);

    for (char ch : html.toCharArray()) {
      String entity = entities.get(ch);
      if (entity != null) {
        sb.append('&');
        sb.append(entity);
        sb.append(';');
      } else {
        sb.append(ch);
      }
    }

    return sb.toString();
  }
}
