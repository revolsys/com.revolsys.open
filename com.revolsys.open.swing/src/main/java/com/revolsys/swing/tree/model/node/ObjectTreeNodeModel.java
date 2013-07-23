package com.revolsys.swing.tree.model.node;

import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeCellRenderer;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public interface ObjectTreeNodeModel<NODE extends Object, CHILD extends Object> {
  int addChild(final NODE node, final CHILD child);

  int addChild(NODE node, int index, CHILD child);

  String convertValueToText(NODE node, boolean selected, boolean expanded,
    boolean leaf, int row, boolean hasFocus);

  CHILD getChild(final NODE node, final int index);

  int getChildCount(final NODE node);

  int getIndexOfChild(final NODE node, final CHILD child);

  Object getLabel(final NODE node);

  MenuFactory getMenu(final NODE node);

  MouseListener getMouseListener(final NODE node);

  ObjectTreeNodeModel<?, ?> getObjectTreeNodeModel(Class<?> clazz);

  List<ObjectTreeNodeModel<?, ?>> getObjectTreeNodeModels();

  TreeCellRenderer getRenderer(final NODE node);

  Set<Class<?>> getSupportedChildClasses();

  Set<Class<?>> getSupportedClasses();

  void initialize(final NODE node);

  boolean isLazyLoad();

  boolean isLeaf(final NODE node);

  boolean removeChild(final NODE node, final CHILD child);

  void setObjectTreeModel(ObjectTreeModel objectTreeModel);
}
