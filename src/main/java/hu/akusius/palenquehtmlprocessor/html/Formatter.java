package hu.akusius.palenquehtmlprocessor.html;

import hu.akusius.palenquehtmlprocessor.HtmlProcessor;
import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import java.util.regex.Pattern;

/**
 *
 * @author Bujdosó Ákos
 */
public final class Formatter extends HtmlProcessor {

  private static final Pattern firstLine = Pattern.compile("<!doctype html><html><head(.*)>");

  @Override
  public boolean conditionalRun() {
    return false;
  }

  @Override
  protected String processHtml(String html, ProcessConfig config) {
    html = firstLine.matcher(html).replaceFirst("<!DOCTYPE html>\n<html>\n  <head$1>");
    html = html.replace("</body></html>", "</body>\n</html>\n");
    return html;
  }
}
