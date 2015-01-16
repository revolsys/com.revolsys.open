package com.revolsys.swing.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import com.revolsys.swing.SwingUtil;

public class SwingAlertAppender extends AppenderSkeleton {

  private boolean hasError = false;

  @Override
  protected void append(final LoggingEvent event) {
    if (event.getLevel().equals(Level.ERROR)) {
      this.hasError = true;
      LoggingEventPanel.showDialog(SwingUtil.getActiveWindow(), event);
    }
  }

  @Override
  public void close() {
  }

  public boolean isHasError() {
    return this.hasError;
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

}
