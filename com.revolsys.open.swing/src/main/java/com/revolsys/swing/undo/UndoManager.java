package com.revolsys.swing.undo;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeSupport;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.util.OperatingSystemUtil;

@SuppressWarnings("serial")
public class UndoManager extends javax.swing.undo.UndoManager implements
  PropertyChangeSupportProxy {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private boolean eventsEnabled = true;

  @Override
  public synchronized boolean addEdit(final UndoableEdit edit) {
    if (edit instanceof AbstractUndoableEdit) {
      final AbstractUndoableEdit abstractEdit = (AbstractUndoableEdit)edit;
      if (!abstractEdit.isHasBeenDone()) {
        abstractEdit.redo();
      }
    }
    if (eventsEnabled) {
      final boolean added = super.addEdit(edit);
      fireEvents();
      return added;
    } else {
      return false;
    }
  }

  public void addKeyMap(final Component component) {
    if (component instanceof JComponent) {
      final JComponent jcomponent = (JComponent)component;
      if (jcomponent instanceof JTextComponent) {
        final JTextComponent textComponent = (JTextComponent)jcomponent;
        textComponent.getDocument().addUndoableEditListener(this);
      }
      final InputMap inputMap = jcomponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      final ActionMap actionMap = jcomponent.getActionMap();

      int modifiers;
      if (OperatingSystemUtil.isMac()) {
        modifiers = Event.META_MASK;
      } else {
        modifiers = Event.CTRL_MASK;
      }
      final KeyStroke undoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifiers);
      final InvokeMethodAction undoAction = new InvokeMethodAction("Undo",
        this, "undo");
      actionMap.put("undo", undoAction);
      inputMap.put(undoKey, "undo");

      final KeyStroke redoKey = KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifiers);
      final InvokeMethodAction redoAction = new InvokeMethodAction("Redo",
        this, "redo");
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
    propertyChangeSupport.firePropertyChange("canUndo", !canUndo, canUndo);
    propertyChangeSupport.firePropertyChange("canRedo", !canRedo, canRedo);
  }

  @Override
  public synchronized PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  public synchronized boolean isCanRedo() {
    return canRedo();
  }

  public synchronized boolean isCanUndo() {
    return canUndo();
  }

  @Override
  public synchronized void redo() throws CannotRedoException {
    final boolean enabled = eventsEnabled;
    try {
      eventsEnabled = false;
      if (canRedo()) {
        super.redo();
      }
    } finally {
      eventsEnabled = enabled;
      fireEvents();
    }
  }

  @Override
  public synchronized void undo() throws CannotUndoException {
    final boolean enabled = eventsEnabled;
    try {
      eventsEnabled = false;
      if (canUndo()) {
        super.undo();
      }
    } finally {
      eventsEnabled = enabled;
      fireEvents();
    }
  }

}
