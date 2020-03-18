package com.revolsys.swing.tree.node;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.Parent;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.util.Property;

public class ParentTreeNode extends LazyLoadTreeNode {
  public ParentTreeNode(final Parent<?> userData) {
    super(userData);
    Property.addListenerRunnable(userData, "children", this::refresh);
  }

  public ParentTreeNode(final Parent<?> userData, final Icon icon) {
    this(userData);
    setIcon(icon);
  }

  @Override
  public boolean getAllowsChildren() {
    final Parent<?> parent = getUserData();
    if (parent != null) {
      return parent.isAllowsChildren();
    }
    return true;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final Parent<?> parent = getUserData();
    if (parent != null) {
      parent.refresh();
      for (final Object child : parent.getChildren()) {
        final BaseTreeNode childNode = BaseTreeNode.newTreeNode(child);
        if (childNode != null) {
          children.add(childNode);
        }
      }
    }
    return children;
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == getUserObject()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("registries")) {
        refresh();
      }
    }
  }

  @Override
  protected synchronized void refreshDo() {
    try {
      final Parent<?> parent = getUserData();
      if (parent != null) {
        parent.refresh();
        super.refreshDo();
      }
    } catch (final Exception e) {
      Logs.error(this, "Error refreshing: " + this, e);
    }
  }
}
