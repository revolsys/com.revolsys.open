package com.revolsys.swing.tree.node;

import javax.swing.Icon;

public class LoadingTreeNode extends BaseTreeNode {
  public LoadingTreeNode(final LazyLoadTreeNode parent) {
    super("Loading...");
    setParent(parent);
  }

  @Override
  public boolean equals(final Object object) {
    return this == object;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    return "Loading...";
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean isUserObjectInitialized() {
    return false;
  }

  @Override
  public String toString() {
    return "Loading...";
  }
}
