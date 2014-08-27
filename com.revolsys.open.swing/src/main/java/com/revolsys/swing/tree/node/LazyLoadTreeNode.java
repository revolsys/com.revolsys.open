package com.revolsys.swing.tree.node;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.ExceptionUtil;

public abstract class LazyLoadTreeNode extends BaseTreeNode {

  private final AtomicInteger updateIndicies = new AtomicInteger();

  private static final BaseTreeNode LOADING_NODE = new BaseTreeNode(
    "Loading...");

  private static final List<BaseTreeNode> LOADING_NODES = Collections.singletonList(LOADING_NODE);

  private List<BaseTreeNode> children = LOADING_NODES;

  private final Object sync = new Object();

  private static Method setChildrenMethod;

  static {
    try {
      final Class<LazyLoadTreeNode> clazz = LazyLoadTreeNode.class;
      setChildrenMethod = clazz.getDeclaredMethod("setChildren", Integer.TYPE,
        List.class);
      setChildrenMethod.setAccessible(true);
    } catch (final Throwable e) {
      ExceptionUtil.log(LazyLoadTreeNode.class, e);
    }
  }

  public LazyLoadTreeNode(final Object userObject) {
    super(userObject, true);
  }

  protected void addNode(final int index, final BaseTreeNode node) {
    final List<BaseTreeNode> children = this.children;
    if (children != LOADING_NODES) {
      children.add(index, node);
    }
  }

  @Override
  protected void doClose() {
    this.children = LOADING_NODES;
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

  public void loadChildren() {
    if (SwingUtilities.isEventDispatchThread()) {
      if (this.children == LOADING_NODES) {
        Invoke.background("Load tree node " + this.getName(), this,
          "loadChildren");
      }
    } else {
      synchronized (this.sync) {
        if (this.children == LOADING_NODES) {
          final int updateIndex = getUpdateIndex();
          List<BaseTreeNode> children = doLoadChildren();
          if (children == null) {
            children = Collections.emptyList();
          }
          Invoke.later(this, setChildrenMethod, updateIndex, children);
        }
      }
    }
  }

  @Override
  public void nodeCollapsed(final BaseTreeNode treeNode) {
    super.nodeCollapsed(treeNode);
    if (treeNode != this) {
      final int updateIndex = getUpdateIndex();
      setChildren(updateIndex, LOADING_NODES);
    }
  }

  public void refresh() {
    if (SwingUtilities.isEventDispatchThread()) {
      Invoke.background("Refresh tree nodes " + this.getName(), this, "refresh");
    } else {
      synchronized (this.sync) {
        final int updateIndex = getUpdateIndex();
        List<BaseTreeNode> children = doLoadChildren();
        if (children == null) {
          children = Collections.emptyList();
        }
        Invoke.later(this, setChildrenMethod, updateIndex, children);
      }
    }
  }

  public final void removeNode(final BaseTreeNode node) {
    final List<BaseTreeNode> children = this.children;
    if (children != LOADING_NODES) {
      final int index = children.indexOf(node);
      removeNode(index);
    }
  }

  public final void removeNode(final int index) {
    final List<BaseTreeNode> children = this.children;
    if (children != LOADING_NODES) {
      if (index > 0 && index < children.size()) {
        final BaseTreeNode node = children.remove(index);
        nodeRemoved(index, node);
      }
    }
  }

  protected void setChildren(final int updateIndex,
    final List<BaseTreeNode> newNodes) {
    synchronized (this.updateIndicies) {
      if (this.children == LOADING_NODES) {
        nodeChanged();
        this.children = Collections.emptyList();
        nodeRemoved(0, LOADING_NODE);
        this.children = newNodes;
        final int[] newIndicies = new int[this.children.size()];
        for (int i = 0; i < newIndicies.length; i++) {
          newIndicies[i] = i;
        }
        for (final BaseTreeNode child : this.children) {
          child.setParent(this);
        }
        nodesInserted(newIndicies);
      } else {
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
  }
}
