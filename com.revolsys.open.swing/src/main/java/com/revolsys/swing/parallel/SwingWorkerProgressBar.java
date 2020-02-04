package com.revolsys.swing.parallel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import com.revolsys.swing.component.BusyLabelPainter;

public class SwingWorkerProgressBar extends JPanel implements PropertyChangeListener {
  private static final long serialVersionUID = -5112492385171847107L;

  private final JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));

  public SwingWorkerProgressBar() {
    super(new BorderLayout(2, 2));
    this.busyLabel.setBusyPainter(new BusyLabelPainter());
    this.busyLabel.setDelay(400);
    this.busyLabel.setFocusable(false);
    Invoke.getPropertyChangeSupport().addPropertyChangeListener("workers", this);
    add(this.busyLabel, BorderLayout.WEST);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    updateVisible();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Invoke.getPropertyChangeSupport().removePropertyChangeListener("workers", this);
  }

  private void updateVisible() {
    if (SwingUtilities.isEventDispatchThread()) {
      final boolean visible = Invoke.hasWorker();
      this.busyLabel.setBusy(visible);
      setVisible(visible);
    } else {
      Invoke.laterQueue(this::updateVisible);
    }
  }
}
