package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ComponentGroup;

@SuppressWarnings("serial")
public class PopupMenu extends JPopupMenu implements MouseListener {
  public static PopupMenu createPopupMenu(final JTextComponent textComponent) {
    synchronized (textComponent) {
      PopupMenu popupMenu = getPopupMenu(textComponent);
      if (popupMenu == null) {
        popupMenu = new PopupMenu();
        popupMenu.addToTextComponent(textComponent);
      }
      return popupMenu;
    }

  }

  public static PopupMenu getPopupMenu(final JComponent component) {
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

  private final ComponentGroup groups = new ComponentGroup();

  public PopupMenu() {
  }

  public PopupMenu(final String title) {
    super(title);
  }

  public void addComponent(final Component component) {
    groups.addComponent(this, component);
  }

  public void addComponent(final String groupName, final Component component) {
    groups.addComponent(this, component);
  }

  public void addGroup(final String groupName) {
    groups.addGroup(groupName);
  }

  public JMenuItem addMenuItem(final Action action) {
    return addMenuItem("default", action);
  }

  public JMenuItem addMenuItem(final JMenuItem menuItem) {
    addComponent(menuItem);
    return menuItem;
  }

  public JMenuItem addMenuItem(final String title) {
    final JMenuItem menuItem = new JMenuItem(title);
    return add(menuItem);
  }

  public JMenuItem addMenuItem(final String groupName, final Action action) {
    final JMenuItem item = super.add(action);
    addComponent(groupName, item);
    return item;
  }

  public void addMenuItem(final String groupName, final JMenuItem menuItem) {
    groups.addComponent(this, groupName, menuItem);
  }

  public JMenuItem addMenuItem(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    final JMenuItem button = createActionComponent(action);
    button.setFocusPainted(false);
    button.setAction(action);
    addComponent(groupName, button);
    return button;
  }

  public JMenuItem addMenuItem(final String groupName, final String title,
    final String iconName, final Object object, final String methodName,
    final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return addMenuItem(groupName, title, title, icon, object, methodName,
      parameters);
  }

  public void addToTextComponent(final JTextComponent textComponent) {
    synchronized (textComponent) {
      addMenuItem("dataTransfer", "Cut", "cut", textComponent, "cut");
      addMenuItem("dataTransfer", "Copy", "page_copy", textComponent, "copy");
      addMenuItem("dataTransfer", "Paste", "paste_plain", textComponent, "paste");

      textComponent.setDragEnabled(true);
      textComponent.addMouseListener(this);
    }
  }

  public List<Component> getGroup(final String groupName) {
    return groups.getGroup(groupName);
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
    if (e.isPopupTrigger()) {
      show(e.getComponent(), e.getX(), e.getY());
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      show(e.getComponent(), e.getX(), e.getY());
    }
  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    groups.setGroupEnabled(groupName, enabled);
  }

}
