package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class PopupMenu implements MouseListener {
  public static PopupMenu getPopupMenu(final JComponent component) {
    synchronized (component) {
      PopupMenu popupMenu = getPopupMenuInternal(component);
      if (popupMenu == null) {
        popupMenu = new PopupMenu();
        popupMenu.addToComponent(component);
      }
      return popupMenu;
    }
  }

  public static MenuFactory getPopupMenuFactory(final JComponent component) {
    synchronized (component) {
      final PopupMenu popupMenu = getPopupMenu(component);
      return popupMenu.getMenu();
    }

  }

  private static PopupMenu getPopupMenuInternal(final JComponent component) {
    synchronized (component) {
      for (final MouseListener mouseListener : component.getMouseListeners()) {
        if (mouseListener instanceof PopupMenu) {
          final PopupMenu popupMenu = (PopupMenu)mouseListener;
          return popupMenu;
        }
      }
      return null;
    }
  }

  private final MenuFactory menu = new MenuFactory();

  public PopupMenu() {
  }

  public boolean addToComponent(final JComponent component) {
    synchronized (component) {
      for (final MouseListener mouseListener : component.getMouseListeners()) {
        if (mouseListener == this) {
          return false;
        } else if (mouseListener instanceof PopupMenu) {
          component.removeMouseListener(mouseListener);
        }
      }
      component.addMouseListener(this);
      if (component instanceof JTextComponent) {
        final MenuFactory menu = getMenu();
        final JTextComponent textComponent = (JTextComponent)component;
        menu.addMenuItemTitleIcon("dataTransfer", "Cut", "cut", textComponent,
          "cut");
        menu.addMenuItemTitleIcon("dataTransfer", "Copy", "page_copy",
          textComponent, "copy");
        menu.addMenuItemTitleIcon("dataTransfer", "Paste", "paste_plain",
          textComponent, "paste");

        textComponent.setDragEnabled(true);
      }
    }
    return true;
  }

  public MenuFactory getMenu() {
    return this.menu;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    showMenu(e);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    showMenu(e);
  }

  protected void showMenu(final MouseEvent e) {
    if (e.isPopupTrigger()) {

      final JPopupMenu popupMenu = this.menu.createJPopupMenu();
      final Component component = e.getComponent();
      final int x = e.getX();
      final int y = e.getY();
      popupMenu.show(component, x, y);
    }
  }

}
