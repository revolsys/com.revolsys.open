package com.revolsys.swing;

import java.awt.ItemSelectable;

import javax.swing.AbstractButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.revolsys.swing.listener.ActionListenable;
import com.revolsys.swing.listener.EventQueueRunnableListener;
import com.revolsys.util.Property;

public interface EventQueue {

  static EventQueueRunnableListener addAction(final AbstractButton component,
    final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    component.addActionListener(listener);
    return listener;
  }

  static EventQueueRunnableListener addAction(final ActionListenable listenable,
    final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    listenable.addActionListener(listener);
    return listener;
  }

  static EventQueueRunnableListener addAction(final JTextField component, final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    component.addActionListener(listener);
    return listener;
  }

  static EventQueueRunnableListener addDocument(final Document document, final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    document.addDocumentListener(listener);
    return listener;
  }

  static EventQueueRunnableListener addDocument(final JTextComponent component,
    final Runnable runnable) {
    final Document document = component.getDocument();
    return addDocument(document, runnable);
  }

  static EventQueueRunnableListener addItem(final ItemSelectable source, final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    source.addItemListener(listener);
    return listener;
  }

  static EventQueueRunnableListener addListSelection(final JList<?> component,
    final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    component.addListSelectionListener(listener);
    return listener;
  }

  static EventQueueRunnableListener addPropertyChange(final Object source,
    final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    Property.addListener(source, listener);
    return listener;
  }

  static EventQueueRunnableListener addPropertyChange(final Object source,
    final String propertyName, final Runnable runnable) {
    final EventQueueRunnableListener listener = new EventQueueRunnableListener(runnable);
    Property.addListener(source, propertyName, listener);
    return listener;
  }

  static Runnable newRunnable(final Runnable runnable) {
    return new EventQueueRunnableListener(runnable);
  }

}
