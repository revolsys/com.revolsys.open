package com.revolsys.swing.parallel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.revolsys.swing.SwingUtil;

public class SwingWorkerProgressBar extends JPanel implements
  PropertyChangeListener {
  private static final long serialVersionUID = -5112492385171847107L;

  private final JProgressBar progressBar = new JProgressBar();

  public SwingWorkerProgressBar() {
    super(new BorderLayout());
    Invoke.getPropertyChangeSupport()
      .addPropertyChangeListener("workers", this);
    progressBar.setIndeterminate(true);
    add(progressBar, BorderLayout.WEST);
    progressBar.setPreferredSize(new Dimension(48, 16));
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final List<?> workers = (List<?>)event.getNewValue();
    SwingUtil.setVisible(progressBar, true);
    if (workers == null || workers.isEmpty()) {
      SwingUtil.setVisible(this, false);
    } else {
      SwingUtil.setVisible(this, true);
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Invoke.getPropertyChangeSupport().removePropertyChangeListener("workers",
      this);
  }
}
