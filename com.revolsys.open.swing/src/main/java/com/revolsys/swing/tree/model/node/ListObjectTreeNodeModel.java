package com.revolsys.swing.tree.model.node;

import java.util.AbstractList;
import java.util.List;

import com.revolsys.swing.tree.model.ObjectTreeModel;

public class ListObjectTreeNodeModel extends
  AbstractObjectTreeNodeModel<List, Object> {
  public ListObjectTreeNodeModel() {
    setSupportedClasses(AbstractList.class);
  }

  public ListObjectTreeNodeModel(final ObjectTreeModel model) {
    super(model);
    setSupportedClasses(AbstractList.class);
  }

  public ListObjectTreeNodeModel(
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    setSupportedClasses(AbstractList.class);
    addObjectTreeNodeModels(objectTreeNodeModels);
  }

  @Override
  public void addObjectTreeNodeModels(
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    super.addObjectTreeNodeModels(objectTreeNodeModels);
  }

  @Override
  protected List<Object> getChildren(final List list) {
    return list;
  }
}
