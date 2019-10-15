package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.parallel.Invoke;

public class BaseJPopupMenu extends JPopupMenu {
  private static final long serialVersionUID = 1L;

  public BaseJPopupMenu() {
  }

  public BaseJPopupMenu(final String name) {
    super(name);
  }

  public JMenuItem addMenuItem(final String title, final Runnable runnable) {
    return addMenuItem(title, null, runnable);
  }

  public JMenuItem addMenuItem(final String title, final String iconName, final Runnable runnable) {
    final JMenuItem menuItem = RunnableAction.newMenuItem(title, iconName, runnable);
    add(menuItem);
    return menuItem;
  }

  public void showMenu(final Component component, final int x, final int y) {
    showMenu((Object)null, component, x, y);
  }

  public boolean showMenu(final MouseEvent e) {
    if (e.isPopupTrigger() && !e.isConsumed()) {
      final Component component = e.getComponent();
      final int x = e.getX();
      final int y = e.getY();
      showMenu(component, x + 5, y);
      return true;
    } else {
      return false;
    }
  }

  public void showMenu(final Object source, final Component component, final int x, final int y) {
    if (SwingUtil.isEventDispatchThread()) {
      final int numItems = getSubElements().length;
      if (numItems > 0) {
        final Window window = SwingUtilities.windowForComponent(component);
        SwingUtil.toFront(window);
        MenuFactory.menuSource = source;
        MenuFactory.currentWindow = window;
        validate();
        addPopupMenuListener(MenuFactory.POPUP_MENU_LISTENER);
        show(component, x, y);
      }
    } else {
      Invoke.later(() -> showMenu(source, component, x, y));
    }
  }
}
