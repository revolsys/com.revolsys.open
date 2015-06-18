package com.revolsys.swing.tree.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.BaseTreeNodeLoadingIcon;

public abstract class LazyLoadTreeNode extends BaseTreeNode {

  private final AtomicInteger updateIndicies = new AtomicInteger();

  private List<BaseTreeNode> children = Collections.emptyList();

  private final Object sync = new Object();

  public LazyLoadTreeNode() {
    this(null);
  }

  public LazyLoadTreeNode(final Object userObject) {
    super(userObject, true);
    setLoading();
  }

  protected void addNode(final int index, final BaseTreeNode node) {
    final List<BaseTreeNode> children = this.children;
    if (isLoaded()) {
      children.add(index, node);
    }
  }

  @Override
  public void collapseChildren() {
    if (isLoaded()) {
      super.collapseChildren();
    }
  }

  private List<BaseTreeNode> createLoadingNodes() {
    final List<BaseTreeNode> nodes = new ArrayList<>();
    nodes.add(new LoadingTreeNode(this));
    return nodes;
  }

  @Override
  protected void doClose() {
    setLoading();
    super.doClose();
  }

  protected List<BaseTreeNode> doLoadChildren() {
    return new ArrayList<BaseTreeNode>();
  }

  @Override
  public List<BaseTreeNode> getChildren() {
    return this.children;
  }

  protected int getUpdateIndex() {
    synchronized (this.updateIndicies) {
      return this.updateIndicies.incrementAndGet();
    }
  }

  public boolean isLoaded() {
    if (this.children.size() == 1) {
      return !(this.children.get(0) instanceof LoadingTreeNode);
    } else {
      return true;
    }
  }

  public void loadChildren() {
    Invoke.background("Load tree node " + this.getName(), () -> {
      synchronized (this.sync) {
        if (!isLoaded()) {
          final int updateIndex = getUpdateIndex();
          List<BaseTreeNode> children = doLoadChildren();
          if (children == null) {
            children = Collections.emptyList();
          }
          final List<BaseTreeNode> childNodes = children;
          Invoke.later(() -> setChildren(updateIndex, childNodes));
        }
      }
    });
  }

  @Override
  public void nodeCollapsed(final BaseTreeNode treeNode) {
    super.nodeCollapsed(treeNode);
    if (treeNode != this) {
      final int updateIndex = getUpdateIndex();
      setChildren(updateIndex, createLoadingNodes());
    }
  }

  public void refresh() {
    Invoke.background("Refresh tree nodes " + this.getName(), () -> {
      synchronized (this.sync) {
        final int updateIndex = getUpdateIndex();
        List<BaseTreeNode> children = doLoadChildren();
        if (children == null) {
          children = Collections.emptyList();
        }
        final List<BaseTreeNode> childNodes = children;
        Invoke.later(() -> setChildren(updateIndex, childNodes));
      }
    });
  }

  public final void removeNode(final BaseTreeNode node) {
    final List<BaseTreeNode> children = this.children;
    if (isLoaded()) {
      final int index = children.indexOf(node);
      removeNode(index);
    }
  }

  public final void removeNode(final int index) {
    final List<BaseTreeNode> children = this.children;
    if (isLoaded()) {
      if (index > 0 && index < children.size()) {
        final BaseTreeNode node = children.remove(index);
        nodeRemoved(index, node);
      }
    }
  }

  protected void setChildren(final int updateIndex, final List<BaseTreeNode> newNodes) {
    synchronized (this.updateIndicies) {
      if (updateIndex == this.updateIndicies.get()) {
        final List<BaseTreeNode> oldNodes = this.children;
        for (int i = 0; i < oldNodes.size();) {
          final BaseTreeNode oldNode = oldNodes.get(i);
          if (newNodes.contains(oldNode)) {
            i++;
          } else {
            oldNodes.remove(i);
            nodeRemoved(i, oldNode);
            oldNode.setParent(null);
            BaseTreeNodeLoadingIcon.removeNode(oldNode);
          }
        }
        for (int i = 0; i < newNodes.size();) {
          final BaseTreeNode oldNode;
          if (i < oldNodes.size()) {
            oldNode = oldNodes.get(i);
          } else {
            oldNode = null;
          }
          final BaseTreeNode newNode = newNodes.get(i);
          if (!newNode.equals(oldNode)) {
            newNode.setParent(this);
            oldNodes.add(i, newNode);
            nodesInserted(i);
          }
          i++;
        }
      }
    }
  }

  private void setLoading() {
    this.children = createLoadingNodes();
  }
}
