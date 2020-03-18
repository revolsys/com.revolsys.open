package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Callable;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.listener.BaseMouseListener;

public class ShowMenuMouseListener implements BaseMouseListener {
  public static ShowMenuMouseListener addListener(final JComponent component,
    final Callable<BaseJPopupMenu> menuFactory) {
    return addListener(component, menuFactory, false);
  }

  public static ShowMenuMouseListener addListener(final JComponent component,
    final Callable<BaseJPopupMenu> menuFactory, final boolean addDnd) {
    if (component != null && menuFactory != null) {
      final ShowMenuMouseListener listener = new ShowMenuMouseListener(component, menuFactory,
        addDnd);
      component.addMouseListener(listener);
      return listener;
    } else {
      return null;
    }
  }

  private Callable<BaseJPopupMenu> menuFactory;

  private JTextComponent textComponent;

  private JComponent component;

  private final boolean addDnd;

  private ShowMenuMouseListener previousListener;

  private ShowMenuMouseListener(final JComponent component,
    final Callable<BaseJPopupMenu> menuFactory, final boolean addDnd) {
    this.component = component;
    this.menuFactory = menuFactory;
    this.addDnd = addDnd;
    addMenuListener(component);
  }

  @SuppressWarnings("rawtypes")
  private boolean addMenuListener(final JComponent component) {
    if (component != null) {
      for (final MouseListener listener : component.getMouseListeners()) {
        if (listener instanceof ShowMenuMouseListener) {
          this.previousListener = (ShowMenuMouseListener)listener;
          component.removeMouseListener(listener);
        }
      }
      component.addMouseListener(this);
      if (component instanceof JComboBox) {
        final JComboBox comboBox = (JComboBox)component;
        final ComboBoxEditor editor = comboBox.getEditor();
        final Component editorComponent = editor.getEditorComponent();
        addMenuListener((JComponent)editorComponent);
      } else if (component instanceof JTextComponent) {
        this.textComponent = (JTextComponent)component;
        this.textComponent.setDragEnabled(true);
      }
    }
    return true;
  }

  public void close() {
    if (this.component != null) {
      this.component.removeMouseListener(this);
      if (this.component instanceof JComboBox) {
        final JComboBox<?> comboBox = (JComboBox<?>)this.component;
        final ComboBoxEditor editor = comboBox.getEditor();
        final Component editorComponent = editor.getEditorComponent();
        editorComponent.removeMouseListener(this);
        if (this.previousListener != null) {
          editorComponent.addMouseListener(this.previousListener);
        }
      }
      if (this.previousListener != null) {
        this.component.addMouseListener(this.previousListener);
      }
    }
    this.previousListener = null;
    this.component = null;
    this.textComponent = null;
    this.menuFactory = null;
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    showMenu(e);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    showMenu(e);
  }

  private void showMenu(final MouseEvent e) {
    if (this.menuFactory != null) {
      if (e.isPopupTrigger() && !e.isConsumed()) {
        e.consume();
        final Component component = e.getComponent();
        final int x = e.getX();
        final int y = e.getY();
        try {
          final BaseJPopupMenu menu = this.menuFactory.call();
          if (menu != null) {
            if (this.addDnd && this.textComponent != null) {
              menu.addSeparator();
              menu.addMenuItem("Cut", "cut", this.textComponent::cut);
              menu.addMenuItem("Copy", "page_copy", this.textComponent::copy);
              menu.addMenuItem("Paste", "paste_plain", this.textComponent::paste);
            }
            menu.showMenu(component, x + 5, y);
          }
        } catch (final Exception e1) {
          Logs.error(this, "Error creating menu", e1);
        }
      }
    }
  }
}
