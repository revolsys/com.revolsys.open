package com.revolsys.logging.log4j;

import javax.swing.JOptionPane;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class SwingAlertAppender extends AppenderSkeleton {

  @Override
  protected void append(final LoggingEvent event) {
    if (event.getLevel().equals(Level.ERROR)) {
      JOptionPane.showMessageDialog(null,
        "The following error occurred, check the log for more details.\n"
          + event.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

}
