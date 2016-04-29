package com.revolsys.swing.tree.node;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.collection.Parent;
import com.revolsys.swing.tree.BaseTreeNode;

public class ParentTreeNode extends LazyLoadTreeNode {
  public ParentTreeNode(final Parent<?> userData) {
    super(userData);
  }

  public ParentTreeNode(final Parent<?> userData, final Icon icon) {
    super(userData);
    setIcon(icon);
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final Parent<?> parent = (Parent<?>)getUserObject();
    final List<BaseTreeNode> children = new ArrayList<>();
    for (final Object child : parent.getChildren()) {
      final BaseTreeNode childNode = BaseTreeNode.newTreeNode(child);
      children.add(childNode);
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
}
