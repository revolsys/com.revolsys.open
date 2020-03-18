package com.revolsys.swing.tree.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;

public abstract class LazyLoadTreeNode extends BaseTreeNode {
  public static void addRefreshMenuItem(final MenuFactory menu) {
    TreeNodes.addMenuItem(menu, "default", "Refresh", "arrow_refresh", LazyLoadTreeNode::refresh);
  }

  private List<BaseTreeNode> children = Collections.emptyList();

  private boolean loaded = false;

  private final AtomicInteger updateIndicies = new AtomicInteger();

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
  protected void closeDo() {
    setLoading();
    super.closeDo();
  }

  @Override
  public void collapseChildren() {
    if (isLoaded()) {
      super.collapseChildren();
    }
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

  @Override
  public boolean isLoaded() {
    return this.loaded;
  }

  public void loadChildren() {
    if (!isLoaded()) {
      this.loaded = true;
      refresh();
    }
  }

  protected List<BaseTreeNode> loadChildrenDo() {
    return new ArrayList<>();
  }

  private List<BaseTreeNode> newLoadingNodes() {
    final List<BaseTreeNode> nodes = new ArrayList<>();
    nodes.add(new LoadingTreeNode(this));
    return nodes;
  }

  @Override
  public void nodeCollapsed(final BaseTreeNode treeNode) {
    super.nodeCollapsed(treeNode);
    if (treeNode != this) {
      final int updateIndex = getUpdateIndex();
      setChildren(updateIndex, newLoadingNodes());
    }
  }

  public final void refresh() {
    Invoke.background("Refresh tree nodes " + this.getName(), this::refreshDo);
  }

  protected synchronized void refreshDo() {
    try {
      final int updateIndex = getUpdateIndex();
      List<BaseTreeNode> children = loadChildrenDo();
      if (children == null) {
        children = Collections.emptyList();
      }
      final List<BaseTreeNode> childNodes = children;
      Invoke.later(() -> setChildren(updateIndex, childNodes));
    } catch (final Throwable e) {
      Logs.error(this, "Error refreshing: " + getName(), e);
    }
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

  private void setChildren(final int updateIndex, final List<BaseTreeNode> newNodes) {
    if (updateIndex == this.updateIndicies.get()) {
      if (newNodes.size() == 1) {
        this.loaded = !(newNodes.get(0) instanceof LoadingTreeNode);
      } else {
        this.loaded = true;
      }
      final List<BaseTreeNode> oldNodes = this.children;
      for (int i = 0; i < oldNodes.size();) {
        final BaseTreeNode oldNode = oldNodes.get(i);
        if (newNodes.contains(oldNode)) {
          i++;
        } else {
          oldNodes.remove(i);
          nodeRemoved(i, oldNode);
          oldNode.setParent(null);
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

  private void setLoading() {
    this.children = newLoadingNodes();
    this.loaded = false;
  }
}
