package com.revolsys.swing.tree.model.node;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.TreePath;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public interface ObjectTreeNodeModel<NODE extends Object, CHILD extends Object> {
  int addChild(NODE node, CHILD child);

  int addChild(NODE node, int index, CHILD child);

  boolean canImport(TreePath path, TransferSupport support);

  String convertValueToText(NODE node, boolean selected, boolean expanded,
    boolean leaf, int row, boolean hasFocus);

  CHILD getChild(NODE node, int index);

  int getChildCount(NODE node);

  int getIndexOfChild(NODE node, CHILD child);

  Object getLabel(NODE node);

  MenuFactory getMenu(NODE node);

  MouseListener getMouseListener(NODE node);

  ObjectTreeNodeModel<?, ?> getObjectTreeNodeModel(Class<?> clazz);

  List<ObjectTreeNodeModel<?, ?>> getObjectTreeNodeModels();

  <T> T getParent(NODE node);

  Component getRenderer(NODE node, JTree tree, boolean selected,
    boolean expanded, boolean leaf, int row, boolean hasFocus);

  Set<Class<?>> getSupportedChildClasses();

  Set<Class<?>> getSupportedClasses();

  void initialize(NODE node);

  boolean isLazyLoad();

  boolean isLeaf(NODE node);

  boolean removeChild(NODE node, CHILD child);

  void setObjectTreeModel(ObjectTreeModel objectTreeModel);
}
