package com.revolsys.swing.tree.model.node;

public class StringTreeNodeModel extends
  AbstractObjectTreeNodeModel<String, Object> {
  public StringTreeNodeModel() {
    setSupportedClasses(String.class);
  }

  @Override
  public boolean isLeaf(final String node) {
    return true;
  }
}
