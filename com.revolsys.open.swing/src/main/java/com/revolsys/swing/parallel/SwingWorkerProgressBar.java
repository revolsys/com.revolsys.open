package com.revolsys.swing.parallel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class SwingWorkerProgressBar extends JPanel implements
  PropertyChangeListener {

  private final JProgressBar progressBar = new JProgressBar();

  public SwingWorkerProgressBar() {
    super(new BorderLayout());
    Invoke.getPropertyChangeSupport().addPropertyChangeListener(
      "workers", this);
    progressBar.setIndeterminate(true);
    add(progressBar, BorderLayout.WEST);
    progressBar.setPreferredSize(new Dimension(48, 16));
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final List<?> workers = (List<?>)event.getNewValue();
    progressBar.setVisible(true);
    if (workers == null || workers.isEmpty()) {
      setVisible(false);
    } else {
      setVisible(true);
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Invoke.getPropertyChangeSupport().removePropertyChangeListener(
      "workers", this);
  }
}
