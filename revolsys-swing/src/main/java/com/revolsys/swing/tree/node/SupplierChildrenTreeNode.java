package com.revolsys.swing.tree.node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.Icon;

import com.revolsys.swing.tree.BaseTreeNode;

public class SupplierChildrenTreeNode extends LazyLoadTreeNode {
  private final Supplier<Iterable<Object>> children;

  public SupplierChildrenTreeNode(final String name, final Icon icon,
    final Supplier<Iterable<Object>> children) {
    super(null);
    setName(name);
    setIcon(icon);
    this.children = children;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> nodes = new ArrayList<>();
    for (final Object child : this.children.get()) {
      final BaseTreeNode node = BaseTreeNode.newTreeNode(child);
      if (node != null) {
        nodes.add(node);
      }
    }
    return nodes;
  }
}
