package com.revolsys.swing.tree.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class TreeEventSupport implements TreeSelectionListener,
  TreeModelListener {
  private final Set<TreeModelListener> modelListeners = new LinkedHashSet<TreeModelListener>();

  private final Set<TreeSelectionListener> selectionListeners = new LinkedHashSet<TreeSelectionListener>();

  public void addTreeModelListener(final TreeModelListener listener) {
    if (listener != null) {
      modelListeners.add(listener);
    }
  }

  public void addTreeSelectionListener(final TreeSelectionListener listener) {
    if (listener != null) {
      selectionListeners.add(listener);
    }
  }

  public void removeTreeModelListener(final TreeModelListener listener) {
    if (listener != null) {
      modelListeners.remove(listener);
    }
  }

  public void removeTreeSelectionListener(final TreeSelectionListener listener) {
    if (listener != null) {
      selectionListeners.remove(listener);
    }
  }

  @Override
  public void treeNodesChanged(final TreeModelEvent e) {
    for (final TreeModelListener listener : modelListeners) {
      listener.treeNodesChanged(e);
    }
  }

  @Override
  public void treeNodesInserted(final TreeModelEvent e) {
    for (final TreeModelListener listener : modelListeners) {
      listener.treeNodesInserted(e);
    }
  }

  @Override
  public void treeNodesRemoved(final TreeModelEvent e) {
    for (final TreeModelListener listener : modelListeners) {
      listener.treeNodesRemoved(e);
    }
  }

  @Override
  public void treeStructureChanged(final TreeModelEvent e) {
    for (final TreeModelListener listener : modelListeners) {
      listener.treeStructureChanged(e);
    }

  }

  @Override
  public void valueChanged(final TreeSelectionEvent e) {
    for (final TreeSelectionListener listener : selectionListeners) {
      listener.valueChanged(e);
    }

  }
}
