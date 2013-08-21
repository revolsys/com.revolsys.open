package com.revolsys.swing.parallel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;

import javax.swing.RootPaneContainer;
import javax.swing.SwingWorker;

import com.revolsys.swing.SwingUtil;
import com.revolsys.util.ExceptionUtil;

public abstract class AbstractSwingWorker<T, V> extends SwingWorker<T, V> {

  private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

  @Override
  protected final void done() {
    final Window activeWindow = SwingUtil.getActiveWindow();
    if (activeWindow != null) {
      Component component;
      Component glassPane = null;
      if (activeWindow instanceof RootPaneContainer) {
        final RootPaneContainer container = (RootPaneContainer)activeWindow;
        glassPane = container.getGlassPane();
        glassPane.setVisible(true);
        component = glassPane;
      } else {
        component = activeWindow;
      }

      final Cursor cursor = activeWindow.getCursor();
      try {
        component.setCursor(WAIT_CURSOR);
        uiTask();
      } catch (final Throwable t) {
        ExceptionUtil.log(getClass(), t);
      } finally {
        if (glassPane != null) {
          glassPane.setVisible(false);
        }
        component.setCursor(cursor);
      }
    }
  }

  protected void uiTask() {
  }
}
