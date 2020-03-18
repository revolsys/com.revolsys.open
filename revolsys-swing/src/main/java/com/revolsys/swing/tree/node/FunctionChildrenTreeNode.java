package com.revolsys.swing.tree.node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.Icon;

import com.revolsys.swing.tree.BaseTreeNode;

public class FunctionChildrenTreeNode extends LazyLoadTreeNode {
  private final Function<Object, Iterable<?>> factory;

  public FunctionChildrenTreeNode(final Object userObject, final String name, final Icon icon,
    final Function<Object, Iterable<?>> factory) {
    super(userObject);
    setName(name);
    setIcon(icon);
    this.factory = factory;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final Object userObject = getUserObject();
    final List<BaseTreeNode> nodes = new ArrayList<>();
    for (final Object child : this.factory.apply(userObject)) {
      final BaseTreeNode node = BaseTreeNode.newTreeNode(child);
      if (node != null) {
        nodes.add(node);
      }
    }
    return nodes;
  }
}
