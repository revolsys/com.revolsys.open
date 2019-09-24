package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.logging.Logs;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Cancellable;

public class ProgressMonitor implements Cancellable {
  private class ProgressMonitorDialog extends JDialog {
    private static final long serialVersionUID = -5843323756390303783L;

    private final JButton cancelButton;

    private final JLabel noteLabel;

    private final JProgressBar progressBar = new JProgressBar();

    private final Timer timer;

    private final JLabel countLabel = new JLabel("0");

    private ProgressMonitorDialog(final Component component, final String title, final String note,
      final boolean canCancel) {
      super(SwingUtil.getWindowAncestor(component), title, ModalityType.APPLICATION_MODAL);
      setMinimumSize(new Dimension(title.length() * 20, 100));
      setLayout(new VerticalLayout(5));
      setMaximumSize(new Dimension(400, 40));
      getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      this.noteLabel = new JLabel(note);
      add(this.noteLabel);

      if (ProgressMonitor.this.max <= 0) {
        this.progressBar.setIndeterminate(true);
      } else {
        final JPanel progressPanel = new JPanel(new BorderLayout());
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(ProgressMonitor.this.max);
        this.progressBar.setMaximumSize(new Dimension());
        progressPanel.add(this.progressBar, BorderLayout.CENTER);
        progressPanel.add(new BasePanel(new FlowLayout(FlowLayout.LEFT, 0, 0), this.countLabel,
          new JLabel(" / " + ProgressMonitor.this.max)), BorderLayout.EAST);
        add(progressPanel);
      }

      final String cancelText = UIManager.getString("OptionPane.cancelButtonText");
      final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      this.cancelButton = RunnableAction.newButton(cancelText, ProgressMonitor.this::cancel);
      this.cancelButton.setEnabled(canCancel);
      buttonPanel.add(this.cancelButton);
      add(buttonPanel);
      setLocationRelativeTo(component);
      pack();
      setAlwaysOnTop(true);
      setResizable(false);
      this.timer = new Timer(500, e -> {
        final int progress = ProgressMonitor.this.progress;
        this.countLabel.setText(Integer.toString(progress));
        this.progressBar.setValue(progress);
      });
    }
  }

  public static void background(final Component component, final String title, final String note,
    final Consumer<ProgressMonitor> task, final int max) {
    background(component, title, note, task, max, (Consumer<Boolean>)null);
  }

  public static void background(final Component component, final String title, final String note,
    final Consumer<ProgressMonitor> task, final int max, final Consumer<Boolean> doneTask) {
    Invoke.later(() -> {
      final ProgressMonitor progressMonitor = new ProgressMonitor(component, title, note, true,
        max);
      Invoke.background(title, () -> {
        try {
          task.accept(progressMonitor);
        } catch (final Throwable e) {
          Logs.error(ProgressMonitor.class, e);
        }
        progressMonitor.progress = progressMonitor.max;
        return null;
      }, r -> {
        progressMonitor.dialog.progressBar.setValue(progressMonitor.progress);
        progressMonitor.setDone();
        component.requestFocus();
        if (doneTask != null) {
          doneTask.accept(!progressMonitor.cancelled);
        }
      });
      progressMonitor.show();
    });
  }

  public static void background(final Component component, final String title, final String note,
    final Consumer<ProgressMonitor> task, final int max, final Runnable doneTask) {
    background(component, title, note, task, max, completed -> doneTask.run());
  }

  public static <V> void background(final String title, final Collection<V> objects,
    final Consumer<V> action) {
    background(SwingUtil.getActiveWindow(), title, null, monitor -> {
      for (final V object : monitor.cancellable(objects)) {
        action.accept(object);
        monitor.addProgress();
      }
    }, objects.size());
  }

  public static <V> void background(final String title, final Collection<V> objects,
    final Consumer<V> action, final Consumer<Boolean> doneTask) {
    background(SwingUtil.getActiveWindow(), title, null, monitor -> {
      for (final V object : monitor.cancellable(objects)) {
        action.accept(object);
        monitor.addProgress();
      }
    }, objects.size(), doneTask);
  }

  public static void background(final String title, final String note,
    final Consumer<ProgressMonitor> task, final int max) {
    background(SwingUtil.getActiveWindow(), title, note, task, max);
  }

  public static void ui(final Component component, final String title, final String note,
    final boolean canCancel, final Consumer<ProgressMonitor> task) {
    Invoke.later(() -> {
      final ProgressMonitor progressMonitor = new ProgressMonitor(component, title, note, canCancel,
        0);
      Invoke.workerDone(title, () -> {
        try {
          task.accept(progressMonitor);
        } catch (final Throwable e) {
          Logs.error(ProgressMonitor.class, e);
        } finally {
          progressMonitor.setDone();
        }
      });
      progressMonitor.show();
    });
  }

  private boolean cancelled = false;

  private final ProgressMonitorDialog dialog;

  private boolean done;

  private final int max;

  private int progress;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private final WindowListener windowListener = new WindowAdapter() {

    @Override
    public void windowOpened(final WindowEvent e) {
      final Window window = e.getWindow();
      window.removeWindowListener(getWindowListener());
      SwingUtil.dispose(ProgressMonitor.this.dialog);
      window.toFront();
    }
  };

  private ProgressMonitor(final Component component, final String title, final String note,
    final boolean canCancel, final int max) {
    this.max = max;
    this.dialog = new ProgressMonitorDialog(component, title, note, canCancel);
  }

  public synchronized void addProgress() {
    if (this.progress < this.max) {
      this.progress++;
      final Timer timer = ProgressMonitor.this.dialog.timer;
      if (!timer.isRunning()) {
        timer.start();
      }
    }
  }

  public void cancel() {
    this.cancelled = true;
    this.propertyChangeSupport.firePropertyChange("cancelled", false, true);
  }

  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public WindowListener getWindowListener() {
    return this.windowListener;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  public boolean isDone() {
    return this.done;
  }

  private void setDone() {
    this.done = true;
    this.dialog.setVisible(false);
  }

  public void setVisible(final Window window) {
    window.addWindowListener(this.windowListener);
    SwingUtil.setVisible(window, true);
  }

  private void show() {
    if (!this.done) {
      this.dialog.setVisible(true);
    }
  }

}
