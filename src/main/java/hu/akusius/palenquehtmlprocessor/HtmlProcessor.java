package hu.akusius.palenquehtmlprocessor;

import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;

/**
 *
 * @author Bujdosó Ákos
 */
public abstract class HtmlProcessor extends Processor<String> {

  @Override
  public Class<String> getType() {
    return String.class;
  }

  @Override
  public final String process(String html, ProcessConfig config) {
    if (isDisabled()) {
      throw new IllegalStateException("Processor is disabled!");
    }
    return processHtml(html, config);
  }

  protected abstract String processHtml(String html, ProcessConfig config);
}
