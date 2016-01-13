package com.revolsys.swing.parallel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;

import org.jdesktop.swingx.JXBusyLabel;

import com.revolsys.swing.SwingUtil;
import com.revolsys.util.Property;

public class SwingWorkerProgressBar extends JPanel implements PropertyChangeListener {
  private static final long serialVersionUID = -5112492385171847107L;

  private final JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));

  public SwingWorkerProgressBar() {
    super(new BorderLayout(2, 2));
    this.busyLabel.setDelay(200);
    Invoke.getPropertyChangeSupport().addPropertyChangeListener("workers", this);
    add(this.busyLabel, BorderLayout.WEST);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final List<?> workers = (List<?>)event.getNewValue();
    boolean visible;
    if (Property.isEmpty(workers)) {
      visible = false;
    } else {
      visible = true;
    }
    this.busyLabel.setBusy(visible);
    SwingUtil.setVisible(this, visible);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Invoke.getPropertyChangeSupport().removePropertyChangeListener("workers", this);
  }
}
