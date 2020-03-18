package com.revolsys.swing.parallel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.RootPaneContainer;
import javax.swing.SwingWorker;

import org.jeometry.common.date.Dates;
import org.jeometry.common.logging.Logs;

import com.revolsys.swing.SwingUtil;
import com.revolsys.util.Cancellable;

public abstract class AbstractSwingWorker<B, V> extends SwingWorker<B, V>
  implements BackgroundTask, Cancellable {
  private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

  private boolean logTimes = false;

  private boolean showBusyCursor = true;

  private String threadName;

  public AbstractSwingWorker() {
  }

  public AbstractSwingWorker(final boolean showBusyCursor) {
    this.showBusyCursor = showBusyCursor;
  }

  private void doDoneTask() {
    try {
      if (isCancelled()) {
        handleCancelled();
      } else {
        try {
          final B result = get();
          if (this.logTimes) {
            final long time = System.currentTimeMillis();
            handleDone(result);
            Dates.printEllapsedTime(toString(), time);
          } else {
            handleDone(result);
          }
        } catch (final CancellationException | InterruptedException e) {
          handleCancelled();
        } catch (final ExecutionException e) {
          handleException(e.getCause());
        } catch (final Throwable e) {
          handleException(e);
        }
      }
    } catch (final Throwable e) {
      Logs.error(getClass(), e);
    } finally {
      try {
        handleFinished();
      } catch (final Throwable e) {
        Logs.error(getClass(), e);
      }
    }
  }

  @Override
  protected final B doInBackground() throws Exception {
    this.threadName = Thread.currentThread()
      .getName()
      .replace("SwingWorker-pool-", "")
      .replace("thread-", "");
    try {
      if (this.logTimes) {
        final long time = System.currentTimeMillis();
        final B result = handleBackground();
        Dates.printEllapsedTime(toString(), time);
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
    final Window activeWindow = SwingUtil.windowActive();
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

  @Override
  public StateValue getTaskStatus() {
    return getState();
  }

  @Override
  public String getTaskThreadName() {
    return this.threadName;
  }

  @Override
  public String getTaskTitle() {
    return toString();
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

  protected void handleFinished() {
  }

  public boolean isShowBusyCursor() {
    return this.showBusyCursor;
  }

  @Override
  public boolean isTaskClosed() {
    return isDone();
  }

  public void setLogTimes(final boolean logTimes) {
    this.logTimes = logTimes;
  }

  protected void setShowBusyCursor(final boolean showBusyCursor) {
    this.showBusyCursor = showBusyCursor;
  }
}
