package com.revolsys.swing.tree.node;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.collection.Parent;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.util.Debug;
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
  protected List<BaseTreeNode> loadChildrenDo() {
    final Parent<?> parent = getUserData();
    final List<BaseTreeNode> children = new ArrayList<>();
    for (final Object child : parent.getChildren()) {
      final BaseTreeNode childNode = BaseTreeNode.newTreeNode(child);
      if (childNode == null) {
        Debug.noOp();
      } else {
        children.add(childNode);
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
    final Parent<?> parent = getUserData();
    if (parent != null) {
      parent.refresh();
      super.refreshDo();
    }
  }
}
