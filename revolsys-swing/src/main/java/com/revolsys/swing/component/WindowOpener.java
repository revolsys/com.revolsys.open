package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXBusyLabel;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.parallel.Invoke;

public class WindowOpener extends JFrame implements WindowListener {
  private static final long serialVersionUID = 1L;

  public static WindowOpener newWindowOpener(final Window window, final String title,
    final String message) {
    return new WindowOpener(window, title, message);
  }

  private WindowOpener(final Window window, final String title, final String message) {
    super(title);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    if (window instanceof JFrame) {
      final JFrame frame = (JFrame)window;
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    final JPanel panel = new JPanel(new BorderLayout());
    setContentPane(panel);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    final JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(16, 16));
    busyLabel.setBusy(true);
    panel.add(busyLabel);
    panel.add(new JLabel(message));

    GroupLayouts.makeColumns(LayoutStyle.getInstance(), panel, 2);
    setMinimumSize(new Dimension(title.length() * 15 + 40, 70));
    pack();
    SwingUtil.setLocationCentre(this);
    setVisible(true);
    SwingUtil.dispose(window);
  }

  public void addOpeningWindow(final Window window) {
    Invoke.later(() -> {
      if (window.isVisible()) {
        setVisible(false);
        window.removeWindowListener(this);
      } else {
        window.addWindowListener(this);
      }
    });
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
    SwingUtil.dispose(this);
  }
}
