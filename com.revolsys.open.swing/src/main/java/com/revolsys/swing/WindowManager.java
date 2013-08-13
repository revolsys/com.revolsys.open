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
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.revolsys.swing.action.InvokeMethodAction;

public class WindowManager implements WindowFocusListener {

  private static final JMenu menu = new JMenu("Window");

  private static final List<Window> windows = new ArrayList<Window>();

  private static final Map<Window, JCheckBoxMenuItem> windowMenuItemMap = new HashMap<Window, JCheckBoxMenuItem>();

  private static final WindowManager INSTANCE = new WindowManager();

  private static Window currentWindow;

  public static void addMenu(final JMenuBar menuBar) {
    menuBar.add(menu);
  }

  public synchronized static void addWindow(final Window window) {
    if (!windows.contains(window)) {
      windows.add(window);
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
      final JCheckBoxMenuItem menuItem = InvokeMethodAction.createCheckBoxMenuItem(
        title, window, "requestFocusInWindow");
      menuItem.setSelected(true);
      menu.add(menuItem);
      windowMenuItemMap.put(window, menuItem);
      window.addWindowFocusListener(INSTANCE);
      window.requestFocusInWindow();
    }
  }

  public synchronized static void removeWindow(final Window window) {
    final JCheckBoxMenuItem menuItem = windowMenuItemMap.get(window);
    if (menuItem != null) {
      menu.remove(menuItem);
      windowMenuItemMap.remove(menuItem);
    }
    windows.remove(menuItem);
    window.removeWindowFocusListener(INSTANCE);
  }

  private WindowManager() {
    // menu.addSeparator();
  }

  @Override
  public void windowGainedFocus(final WindowEvent e) {
    final Window window = e.getWindow();
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
