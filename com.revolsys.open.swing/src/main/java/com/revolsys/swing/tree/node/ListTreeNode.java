package com.revolsys.swing.tree.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.BaseTreeNodeLoadingIcon;

public class ListTreeNode extends BaseTreeNode {
  private final List<BaseTreeNode> children = new ArrayList<>();

  private final List<BaseTreeNode> publicChildren = Collections.unmodifiableList(this.children);

  public ListTreeNode() {
    this(null, null);
  }

  public ListTreeNode(final BaseTreeNode... nodes) {
    this(null, Arrays.asList(nodes));
  }

  public ListTreeNode(final BaseTreeNode node) {
    this(null, Arrays.asList(node));
  }

  public ListTreeNode(final Object userData) {
    this(userData, null);
  }

  public ListTreeNode(final Object userObject,
    List<? extends BaseTreeNode> children) {
    super(userObject, true);
    if (children == null) {
      children = doLoadChildren();
    }
    setChildren(children);
  }

  public void addNode(final BaseTreeNode child) {
    if (child != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final int index = this.children.size();
        this.children.add(child);
        child.setParent(this);
        nodesInserted(index);
      } else {
        Invoke.andWait(this, "addNode", child);
      }
    }
  }

  protected List<BaseTreeNode> doLoadChildren() {
    return this.children;
  }

  @Override
  public List<BaseTreeNode> getChildren() {
    return this.publicChildren;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  public void refresh() {
    if (SwingUtilities.isEventDispatchThread()) {
      final List<BaseTreeNode> newChildren = doLoadChildren();
      setChildren(newChildren);
    } else {
      Invoke.andWait(this, "refresh");
    }
  }

  public void removeNode(final int index) {
    if (SwingUtilities.isEventDispatchThread()) {
      if (index >= 0 && index < this.children.size()) {
        final BaseTreeNode node = this.children.remove(index);
        node.setParent(null);
        nodeRemoved(index, node);
      }
    } else {
      Invoke.andWait(this, "removeNode", index);
    }
  }

  public void setChildren(final List<? extends BaseTreeNode> children) {
    final List<BaseTreeNode> oldChildren = this.children;
    if (children != oldChildren) {
      for (int i = 0; i < oldChildren.size();) {
        final BaseTreeNode oldChild = oldChildren.get(i);
        if (children.contains(oldChild)) {
          i++;
        } else {
          oldChildren.remove(i);
          nodeRemoved(i, oldChild);
          oldChild.setParent(null);
          BaseTreeNodeLoadingIcon.removeNode(oldChild);
        }
      }
      int i = 0;
      while (i < children.size() && i < oldChildren.size()) {
        final BaseTreeNode oldChild = oldChildren.get(i);
        final BaseTreeNode newNode = children.get(i);
        if (!newNode.equals(oldChild)) {
          newNode.setParent(this);
          oldChildren.add(i, newNode);
          nodesInserted(i);
        }
        i++;
      }
      while (i < children.size()) {
        final BaseTreeNode newNode = children.get(i);
        newNode.setParent(this);
        oldChildren.add(i, newNode);
        nodesInserted(i);
        i++;
      }
    }
  }
}
