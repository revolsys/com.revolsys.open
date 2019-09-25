package com.revolsys.swing.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

public class SwingAlertAppender extends BaseAppender {

  private boolean hasError = false;

  public SwingAlertAppender() {
    setName("swing");
  }

  @Override
  public void append(final LogEvent event) {
    if (event.getLevel().equals(Level.ERROR)) {
      this.hasError = true;
      LoggingEventPanel.showDialog(event);
    }
  }

  public boolean isHasError() {
    return this.hasError;
  }

}
