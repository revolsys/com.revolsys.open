package com.revolsys.swing.undo;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.util.OS;
import com.revolsys.value.GlobalBooleanValue;

public class UndoManager extends javax.swing.undo.UndoManager
  implements PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  private final GlobalBooleanValue eventsEnabled = new GlobalBooleanValue(true);

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  @Override
  public synchronized boolean addEdit(final UndoableEdit edit) {
    if (edit == null) {
      return false;
    } else {
      final boolean enabled = isEventsEnabled();
      if (enabled) {
        try (
          BaseCloseable c = setEventsEnabled(false)) {
          if (edit instanceof AbstractUndoableEdit) {
            final AbstractUndoableEdit abstractEdit = (AbstractUndoableEdit)edit;
            if (!abstractEdit.isHasBeenDone()) {
              if (abstractEdit.canRedo()) {
                abstractEdit.redo();
              } else {
                return false;
              }
            }
          }
        }
        if (isEventsEnabled()) {
          final boolean added = super.addEdit(edit);
          fireEvents();
          return added;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  public void addKeyMap(final Component component) {
    if (component instanceof JComponent) {
      final JComponent jcomponent = (JComponent)component;
      if (jcomponent instanceof JTextComponent) {
        final JTextComponent textComponent = (JTextComponent)jcomponent;
        textComponent.getDocument().addUndoableEditListener(this);
      }
      final InputMap inputMap = jcomponent
        .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      final ActionMap actionMap = jcomponent.getActionMap();

      int modifiers;
      if (OS.isMac()) {
        modifiers = Event.META_MASK;
      } else {
        modifiers = Event.CTRL_MASK;
      }
      final KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifiers);
      final RunnableAction undoAction = new RunnableAction("Undo", this::undo);
      actionMap.put("undo", undoAction);
      inputMap.put(undoKey, "undo");

      final KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifiers);
      final RunnableAction redoAction = new RunnableAction("Redo", this::redo);
      actionMap.put("redo", redoAction);
      inputMap.put(redoKey, "redo");

    }
  }

  @Override
  public synchronized void discardAllEdits() {
    super.discardAllEdits();
    fireEvents();
  }

  protected synchronized void fireEvents() {
    final boolean canUndo = isCanUndo();
    final boolean canRedo = isCanRedo();
    this.propertyChangeSupport.firePropertyChange("canUndo", !canUndo, canUndo);
    this.propertyChangeSupport.firePropertyChange("canRedo", !canRedo, canRedo);
  }

  @Override
  public synchronized PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public synchronized boolean isCanRedo() {
    return canRedo();
  }

  public synchronized boolean isCanUndo() {
    return canUndo();
  }

  public boolean isEventsEnabled() {
    return this.eventsEnabled.isTrue();
  }

  @Override
  public synchronized void redo() throws CannotRedoException {
    try (
      BaseCloseable c = setEventsEnabled(false)) {
      if (canRedo()) {
        super.redo();
      }
    } finally {
      fireEvents();
    }
  }

  public BaseCloseable setEventsEnabled(final boolean eventsEnabled) {
    return this.eventsEnabled.closeable(eventsEnabled);
  }

  @Override
  public synchronized void undo() throws CannotUndoException {
    try (
      BaseCloseable c = setEventsEnabled(false)) {
      if (canUndo()) {
        super.undo();
      }
    } finally {
      fireEvents();
    }
  }
}
