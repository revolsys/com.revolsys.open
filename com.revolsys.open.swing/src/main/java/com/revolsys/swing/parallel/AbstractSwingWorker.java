package com.revolsys.swing.parallel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.RootPaneContainer;
import javax.swing.SwingWorker;

import com.revolsys.logging.Logs;
import com.revolsys.swing.SwingUtil;

public abstract class AbstractSwingWorker<B, V> extends SwingWorker<B, V> {
  private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

  private final boolean logTimes = false;

  private boolean showBusyCursor = true;

  private String threadName;

  public AbstractSwingWorker() {
  }

  public AbstractSwingWorker(final boolean showBusyCursor) {
    this.showBusyCursor = showBusyCursor;
  }

  private void doDoneTask() {
    if (isCancelled()) {
      handleCancelled();
    } else {
      try {
        final B result = get();
        if (this.logTimes) {
          final long time = System.currentTimeMillis();
          handleDone(result);
        } else {
          handleDone(result);
        }
      } catch (final CancellationException e) {
        handleCancelled();
      } catch (final InterruptedException t) {
        handleCancelled();
      } catch (final ExecutionException e) {
        handleException(e.getCause());
      } catch (final Throwable e) {
        handleException(e);
      }
    }
  }

  @Override
  protected final B doInBackground() throws Exception {
    this.threadName = Thread.currentThread().getName().replace("SwingWorker-pool-", "").replace(
      "thread-", "");
    try {
      if (this.logTimes) {
        final long time = System.currentTimeMillis();
        final B result = handleBackground();
        return result;
      } else {
        return handleBackground();
      }
    } finally {
      this.threadName = null;
    }
  }

  @Override
  protected final void done() {
    final Window activeWindow = SwingUtil.getActiveWindow();
    if (isShowBusyCursor() && activeWindow != null) {
      Component component;
      Component glassPane = null;
      if (activeWindow instanceof RootPaneContainer) {
        final RootPaneContainer container = (RootPaneContainer)activeWindow;
        glassPane = container.getGlassPane();
        SwingUtil.setVisible(glassPane, true);
        component = glassPane;
      } else {
        component = activeWindow;
      }

      final Cursor cursor = activeWindow.getCursor();
      try {
        component.setCursor(WAIT_CURSOR);
        doDoneTask();
      } finally {
        if (glassPane != null) {
          SwingUtil.setVisible(glassPane, false);
        }
        component.setCursor(cursor);
      }
    } else {
      doDoneTask();
    }
  }

  public String getThreadName() {
    return this.threadName;
  }

  protected B handleBackground() {
    return null;
  }

  protected void handleCancelled() {
  }

  protected void handleDone(final B result) {
  }

  protected void handleException(final Throwable exception) {
    Logs.error(this, "Unable to execute:" + this, exception);
  }

  public boolean isShowBusyCursor() {
    return this.showBusyCursor;
  }

  protected void setShowBusyCursor(final boolean showBusyCursor) {
    this.showBusyCursor = showBusyCursor;
  }
}
