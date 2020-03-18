package com.revolsys.swing.tree.node;

import javax.swing.Icon;

import com.revolsys.swing.tree.BaseTreeNode;

public class LoadingTreeNode extends BaseTreeNode {
  public LoadingTreeNode(final LazyLoadTreeNode parent) {
    super("Loading...");
    setParent(parent);
  }

  @Override
  protected void addListener() {
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
  protected void removeListener() {
  }

  @Override
  public String toString() {
    return "Loading...";
  }
}
