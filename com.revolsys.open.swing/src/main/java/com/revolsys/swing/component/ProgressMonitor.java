package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.parallel.Invoke;

public class ProgressMonitor extends JDialog implements WindowListener {
  private static final long serialVersionUID = -5843323756390303783L;

  public static void background(final Component component, final String title, final String note,
    final boolean canCancel, final Object object, final String methodName,
    final Object... parameters) {
    final ProgressMonitor progressMonitor = new ProgressMonitor(component, title, note, canCancel);
    final List<Object> params = new ArrayList<Object>();
    params.add(progressMonitor);
    params.addAll(Arrays.asList(parameters));
    Invoke.background(title, object, methodName, params);
    progressMonitor.setVisible(true);
  }

  public static void ui(final Component component, final String title, final String note,
    final boolean canCancel, final Object object, final String methodName,
    final Object... parameters) {
    final ProgressMonitor progressMonitor = new ProgressMonitor(component, title, note, canCancel);
    final List<Object> params = new ArrayList<Object>();
    params.add(progressMonitor);
    params.addAll(Arrays.asList(parameters));
    Invoke.worker(title, object, null, null, methodName, params);
    progressMonitor.setVisible(true);
  }

  private final JButton cancelButton;

  private boolean cancelled = false;

  private final JLabel noteLabel;

  private final JProgressBar progressBar = new JProgressBar();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private ProgressMonitor(final Component component, final String title, final String note,
    final boolean canCancel) {
    this(component, title, note, canCancel, 0, 0);
  }

  private ProgressMonitor(final Component component, final String title, final String note,
    final boolean canCancel, final int min, final int max) {
    super(SwingUtil.getWindowAncestor(component), title, ModalityType.APPLICATION_MODAL);
    setMinimumSize(new Dimension(title.length() * 20, 100));
    setLayout(new VerticalLayout(5));
    getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.noteLabel = new JLabel(note);
    add(this.noteLabel);
    if (max <= 0) {
      this.progressBar.setIndeterminate(true);
    } else {
      this.progressBar.setMinimum(min);
      this.progressBar.setMaximum(min);
    }
    final String cancelText = UIManager.getString("OptionPane.cancelButtonText");
    final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    this.cancelButton = InvokeMethodAction.createButton(cancelText, this, "cancel");
    this.cancelButton.setEnabled(canCancel);
    buttonPanel.add(this.cancelButton);
    add(this.progressBar);
    add(buttonPanel);
    setLocationRelativeTo(component);
    pack();
    setResizable(false);
  }

  public void cancel() {
    this.cancelled = true;
    this.propertyChangeSupport.firePropertyChange("cancelled", false, true);
  }

  public void close() {
    if (SwingUtilities.isEventDispatchThread()) {
      setVisible(false);
      dispose();
    } else {
      Invoke.later(this, "close");
    }
  }

  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public boolean isCancelled() {
    return this.cancelled;
  }

  public void setNote(final String note) {
    this.noteLabel.setText(note);
    pack();
  }

  public void setProgress(final int newValue) {
    this.progressBar.setValue(newValue);
  }

  public void setVisible(final Window window) {
    window.addWindowListener(this);
    SwingUtil.setVisible(window, true);
  }

  @Override
  public void windowActivated(final WindowEvent e) {
  }

  @Override
  public void windowClosed(final WindowEvent e) {
  }

  @Override
  public void windowClosing(final WindowEvent e) {
  }

  @Override
  public void windowDeactivated(final WindowEvent e) {
  }

  @Override
  public void windowDeiconified(final WindowEvent e) {
  }

  @Override
  public void windowIconified(final WindowEvent e) {
  }

  @Override
  public void windowOpened(final WindowEvent e) {
    final Window window = e.getWindow();
    window.removeWindowListener(this);
    close();
    window.toFront();
  }

}
