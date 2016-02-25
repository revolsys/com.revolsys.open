package com.revolsys.swing;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.revolsys.swing.action.RunnableAction;

public class WindowManager implements WindowFocusListener {

  private static Window currentWindow;

  private static final WindowManager INSTANCE = new WindowManager();

  private static final JMenu menu = new JMenu("Window");

  private static final Map<Window, JCheckBoxMenuItem> windowMenuItemMap = new HashMap<>();

  private static final List<Window> windows = new ArrayList<>();

  public static void addMenu(final JMenuBar menuBar) {
    menuBar.add(menu);
  }

  public synchronized static void addWindow(final Window window) {
    if (!windows.contains(window)) {
      windows.add(window);
      final String title = getTitle(window);
      final JCheckBoxMenuItem menuItem = RunnableAction.newCheckBoxMenuItem(title,
        () -> requestFocus(window));
      menuItem.setSelected(true);
      menu.add(menuItem);
      windowMenuItemMap.put(window, menuItem);
      window.addWindowFocusListener(INSTANCE);
      window.requestFocus();
    }
  }

  protected static String getTitle(final Window window) {
    String title = window.getName();
    if (window instanceof Frame) {
      final Frame frame = (Frame)window;
      title = frame.getTitle();
    } else if (window instanceof Dialog) {
      final Dialog dialog = (Dialog)window;
      title = dialog.getTitle();
    } else {
      title = window.getName();
    }
    return title;
  }

  public synchronized static void removeWindow(final Window window) {
    if (window != null) {
      final JCheckBoxMenuItem menuItem = windowMenuItemMap.remove(window);
      if (menuItem != null) {
        menuItem.setAction(null);
        menu.remove(menuItem);
      }
      windows.remove(window);
      window.removeWindowFocusListener(INSTANCE);
      if (window == currentWindow) {
        currentWindow = null;
      }
    }
  }

  public static void requestFocus(final Window window) {
    if (window != null) {
      window.requestFocus();
      final JCheckBoxMenuItem menuItem = windowMenuItemMap.get(window);
      if (menuItem != null) {
        menuItem.setSelected(true);
      }
    }
  }

  public synchronized static void updateWindowTitle(final Window window) {
    if (window != null) {
      final JCheckBoxMenuItem menuItem = windowMenuItemMap.get(window);
      if (menuItem != null) {
        final String title = getTitle(window);
        menuItem.setText(title);
      }
    }
  }

  private WindowManager() {
  }

  @Override
  public void windowGainedFocus(final WindowEvent e) {
    final Window window = e.getWindow();
    if (window instanceof JFrame) {
      final JFrame frame = (JFrame)window;
      JMenuBar menuBar = frame.getJMenuBar();
      if (menuBar == null) {
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
      }
      addMenu(menuBar);
    }
    final JCheckBoxMenuItem menuItem = windowMenuItemMap.get(window);
    if (menuItem != null) {
      menuItem.setSelected(true);
    }
    currentWindow = window;
  }

  @Override
  public void windowLostFocus(final WindowEvent e) {
    final Window window = e.getWindow();

    final JCheckBoxMenuItem menuItem = windowMenuItemMap.get(window);
    if (menuItem != null) {
      menuItem.setSelected(false);
    }
    if (window == currentWindow) {
      currentWindow = null;
    }
  }
}
