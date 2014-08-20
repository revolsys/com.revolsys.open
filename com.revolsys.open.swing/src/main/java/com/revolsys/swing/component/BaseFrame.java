package com.revolsys.swing.component;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.revolsys.swing.WindowManager;

@SuppressWarnings("serial")
public class BaseFrame extends JFrame implements WindowListener {

  public BaseFrame() throws HeadlessException {
    super();
    init();
  }

  public BaseFrame(final GraphicsConfiguration gc) {
    super(gc);
    init();
  }

  public BaseFrame(final String title) throws HeadlessException {
    super(title);
    init();
  }

  public BaseFrame(final String title, final GraphicsConfiguration gc) {
    super(title, gc);
    init();
  }

  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    WindowManager.addMenu(menuBar);
    return menuBar;
  }

  @Override
  public void dispose() {
    removeWindowListener(this);
    WindowManager.removeWindow(this);
    super.dispose();
  }

  private void init() {
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(this);
    createMenuBar();
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible) {
      WindowManager.addWindow(this);
    } else {
      WindowManager.removeWindow(this);
    }
    super.setVisible(visible);
  }

  @Override
  public void windowActivated(final WindowEvent e) {
  }

  @Override
  public void windowClosed(final WindowEvent e) {
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    setVisible(false);
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
  }

}
