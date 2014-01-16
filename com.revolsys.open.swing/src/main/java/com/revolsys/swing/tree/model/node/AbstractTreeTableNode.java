package com.revolsys.swing.tree.model.node;

import java.util.Enumeration;

import org.jdesktop.swingx.treetable.TreeTableNode;

public abstract class AbstractTreeTableNode extends AbstractTreeNode implements
  TreeTableNode {

  public AbstractTreeTableNode(final Object userObject) {
    super(userObject);
  }

  public AbstractTreeTableNode(final Object userObject,
    final boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  public AbstractTreeTableNode(final TreeTableNode parent,
    final Object userObject) {
    super(parent, userObject);
  }

  public AbstractTreeTableNode(final TreeTableNode parent,
    final Object userObject, final boolean allowsChildren) {
    super(parent, userObject, allowsChildren);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumeration<? extends TreeTableNode> children() {
    return super.children();
  }

  @Override
  public TreeTableNode getChildAt(final int index) {
    return (TreeTableNode)super.getChildAt(index);
  }

  @Override
  public int getColumnCount() {
    return 0;
  }

  @Override
  public TreeTableNode getParent() {
    return (TreeTableNode)super.getParent();
  }

  @Override
  public Object getUserObject() {
    return super.getUserObject();
  }

  @Override
  public Object getValueAt(final int columnIndex) {
    return null;
  }

  @Override
  public boolean isEditable(final int column) {
    return false;
  }

  @Override
  public void setValueAt(final Object value, final int columnIndex) {
  }

}
