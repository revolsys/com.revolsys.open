package com.revolsys.swing.component;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

import com.revolsys.logging.Slf4jUncaughtExceptionHandler;
import com.revolsys.swing.WindowManager;

@SuppressWarnings("serial")
public class BaseFrame extends JFrame {
  static {
    Slf4jUncaughtExceptionHandler.init();
  }

  public BaseFrame() throws HeadlessException {
    super();
  }

  public BaseFrame(final GraphicsConfiguration gc) {
    super(gc);
  }

  public BaseFrame(final String title) throws HeadlessException {
    super(title);
  }

  public BaseFrame(final String title, final GraphicsConfiguration gc) {
    super(title, gc);
  }

  @Override
  public void dispose() {
    WindowManager.removeWindow(this);
    super.dispose();
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

}
