package com.revolsys.swing.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class SwingAlertAppender extends AppenderBase<ILoggingEvent> {

  private boolean hasError = false;

  public SwingAlertAppender() {
    setName("swing");
  }

  @Override
  protected void append(final ILoggingEvent event) {
    if (event.getLevel().equals(Level.ERROR)) {
      this.hasError = true;
      LoggingEventPanel.showDialog(event);
    }
  }

  public boolean isHasError() {
    return this.hasError;
  }

}
