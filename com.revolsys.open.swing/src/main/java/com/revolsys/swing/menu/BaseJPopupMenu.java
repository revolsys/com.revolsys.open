package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;

public class BaseJPopupMenu extends JPopupMenu {
  private static final long serialVersionUID = 1L;

  public static BaseJPopupMenu showMenu(final Supplier<BaseJPopupMenu> menuConstructor,
    final MenuSourceHolder menuSourceHolder, final Component component, final int x, final int y) {
    final BaseJPopupMenu menu = menuConstructor.get();
    if (menu == null) {
      menuSourceHolder.close();
    } else {
      menu.showMenu(menuSourceHolder, component, x, y);
    }
    return menu;
  }

  public static void showMenu(final Supplier<BaseJPopupMenu> menuConstructor,
    final MenuSourceHolder menuSourceHolder, final Component component, final MouseEvent event) {
    final int x = event.getX();
    final int y = event.getY();
    showMenu(menuConstructor, menuSourceHolder, component, x, y);
  }

  public static BaseJPopupMenu showMenu(final Supplier<BaseJPopupMenu> menuConstructor,
    final Object menuSource, final Component component, final int x, final int y) {
    try (
      MenuSourceHolder menuSourceHolder = new MenuSourceHolder(menuSource)) {
      return showMenu(menuConstructor, menuSourceHolder, component, x, y);
    }
  }

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

  public void showMenu(final MenuSourceHolder menuSourceHolder, final Component component,
    final int x, final int y) {
    final int numItems = getSubElements().length;
    if (numItems > 0) {
      final Window window = SwingUtilities.windowForComponent(component);
      SwingUtil.toFront(window);
      menuSourceHolder.setWindow(window);
      validate();
      addPopupMenuListener(new PopupMenuListener() {

        @Override
        public void popupMenuCanceled(final PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
          // menuSourceHolder.closeDo();
        }

        @Override
        public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
          menuSourceHolder.setWindow(null);
        }
      });
      menuSourceHolder.setMenuVisible(true);
      show(component, x, y);
    }
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
    try (
      MenuSourceHolder menuSourceHolder = new MenuSourceHolder(source)) {
      showMenu(menuSourceHolder, component, x, y);
    }
  }

}
