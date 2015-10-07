package hu.akusius.palenquehtmlprocessor;

import hu.akusius.palenquehtmlprocessor.config.ProcessConfig;
import hu.akusius.palenquehtmlprocessor.config.SessionConfig;

/**
 *
 * @author Bujdosó Ákos
 * @param <T>
 */
public abstract class Processor<T> {

  public abstract Class<T> getType();

  public void sessionStart(SessionConfig config) {
    if (isDisabled()) {
      throw new IllegalStateException("Processor is disabled!");
    }
  }

  public abstract T process(T object, ProcessConfig config);

  public void sessionEnd(SessionConfig config) {
    if (isDisabled()) {
      throw new IllegalStateException("Processor is disabled!");
    }
  }

  public String getParamName() {
    return getClass().getSimpleName();
  }

  public boolean conditionalRun() {
    return true;
  }

  private boolean disabled = false;

  public final boolean isDisabled() {
    return disabled;
  }

  public final void disable() {
    if (!conditionalRun()) {
      throw new IllegalStateException("Processor cannot be disabled!");
    }
    this.disabled = true;
  }
}
