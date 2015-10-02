package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import com.revolsys.swing.field.Field;

public class PopupMenu implements MouseListener {
  public static PopupMenu getPopupMenu(final JComponent component) {
    synchronized (component) {
      PopupMenu popupMenu = getPopupMenuInternal(component);
      if (popupMenu == null) {
        String name = "Field";
        if (component instanceof Field) {
          final Field field = (Field)component;
          name += " " + field.getFieldName();
        }
        popupMenu = new PopupMenu(name);
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

  @SuppressWarnings("rawtypes")
  public static void removeFromComponent(final JComponent component) {
    for (final MouseListener mouseListener : component.getMouseListeners()) {
      if (mouseListener instanceof PopupMenu) {
        component.removeMouseListener(mouseListener);
      }
    }
    if (component instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)component;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component editorComponent = editor.getEditorComponent();
      removeFromComponent((JComponent)editorComponent);
    }
  }

  private boolean autoCreateDnd = true;

  private final MenuFactory menu;

  public PopupMenu(final MenuFactory menu) {
    this.menu = menu;
  }

  public PopupMenu(final String name) {
    this(new MenuFactory(name));
  }

  @SuppressWarnings("rawtypes")
  public boolean addToComponent(final JComponent component) {
    synchronized (component) {
      removeFromComponent(component);
      component.addMouseListener(this);
      if (component instanceof JComboBox) {
        final JComboBox comboBox = (JComboBox)component;
        final ComboBoxEditor editor = comboBox.getEditor();
        final Component editorComponent = editor.getEditorComponent();
        addToComponent((JComponent)editorComponent);
      }
      if (component instanceof JTextComponent) {
        final MenuFactory menu = getMenu();
        final JTextComponent textComponent = (JTextComponent)component;
        if (this.autoCreateDnd) {
          if (!menu.getProperty("hasDndMenu", Boolean.FALSE)) {
            menu.setProperty("hasDndMenu", Boolean.TRUE);
            menu.addMenuItemTitleIcon("dataTransfer", "Cut", "cut", textComponent::cut);
            menu.addMenuItemTitleIcon("dataTransfer", "Copy", "page_copy", textComponent::copy);
            menu.addMenuItemTitleIcon("dataTransfer", "Paste", "paste_plain", textComponent::paste);
          }
        }
        textComponent.setDragEnabled(true);
      }
    }
    return true;
  }

  public MenuFactory getMenu() {
    return this.menu;
  }

  public boolean isAutoCreateDnd() {
    return this.autoCreateDnd;
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

  public void setAutoCreateDnd(final boolean autoCreateDnd) {
    this.autoCreateDnd = autoCreateDnd;
  }

  public void show(final Component component, final int x, final int y) {
    this.menu.show(null, component, x, y);
  }

  protected void showMenu(final MouseEvent e) {
    if (e.isPopupTrigger() && !e.isConsumed()) {
      final Component component = e.getComponent();
      final int x = e.getX();
      final int y = e.getY();
      show(component, x + 5, y);
    }
  }

}
