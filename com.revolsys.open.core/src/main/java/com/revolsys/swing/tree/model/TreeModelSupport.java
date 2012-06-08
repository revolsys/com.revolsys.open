package com.revolsys.swing.tree.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class TreeModelSupport {
  private final List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

  public void addTreeModelListener(final TreeModelListener listener) {
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void fireTreeNodesChanged(final TreeModelEvent e) {
    for (final TreeModelListener listener : listeners) {
      listener.treeNodesChanged(e);
    }
  }

  public void fireTreeNodesInserted(final TreeModelEvent e) {
    for (final TreeModelListener listener : listeners) {
      listener.treeNodesInserted(e);
    }
  }

  public void fireTreeNodesRemoved(final TreeModelEvent e) {
    for (final TreeModelListener listener : listeners) {
      listener.treeNodesRemoved(e);
    }
  }

  public void fireTreeStructureChanged(final TreeModelEvent e) {
    for (final TreeModelListener listener : listeners) {
      listener.treeStructureChanged(e);
    }

  }

  public void removeTreeModelListener(final TreeModelListener listener) {
    if (listener != null) {
      listeners.remove(listener);
    }
  }
}
