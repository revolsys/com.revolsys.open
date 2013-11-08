package com.revolsys.swing.tree.model.node;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.BaseTree;

public abstract class LazyLoadTreeNode extends AbstractTreeNode {

  private static final DefaultMutableTreeNode LOADING_NODE = new DefaultMutableTreeNode(
    "Loading...");

  private static final List<TreeNode> LOADING_NODES = Collections.<TreeNode> singletonList(LOADING_NODE);

  private List<TreeNode> children = LOADING_NODES;

  private final Object sync = new Object();

  private static Method setChildrenMethod;

  static {
    LOADING_NODE.setAllowsChildren(false);
    try {
      final Class<LazyLoadTreeNode> clazz = LazyLoadTreeNode.class;
      setChildrenMethod = clazz.getDeclaredMethod("setChildren",
        BaseTree.class, List.class);
      setChildrenMethod.setAccessible(true);
    } catch (final SecurityException e) {
    } catch (final NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public LazyLoadTreeNode(final TreeNode parent, final Object userObject) {
    super(parent, userObject);

  }

  protected List<TreeNode> doLoadChildren() {
    return new ArrayList<TreeNode>();
  }

  @Override
  public int getChildCount() {
    return getChildren().size();
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }

  public void loadChildren(final BaseTree tree) {
    if (SwingUtilities.isEventDispatchThread()) {
      if (children == LOADING_NODES) {
        Invoke.background("Load tree node " + this.getName(), this,
          "loadChildren", tree);
      }
    } else {
      synchronized (sync) {
        if (children == LOADING_NODES) {
          final List<TreeNode> children = doLoadChildren();
          this.children = children.subList(0, 1);
          Invoke.later(this, setChildrenMethod, tree, children);

        }
      }
    }
  }

  protected void setChildren(final BaseTree tree, final List<TreeNode> children) {

    final TreeModel model = tree.getModel();
    if (model instanceof DefaultTreeModel) {
      final DefaultTreeModel treeModel = (DefaultTreeModel)model;
      treeModel.nodeChanged(this);
      treeModel.nodesChanged(this, new int[] {
        0
      });
      this.children = children;
      final int[] newIndicies = new int[children.size() - 1];
      for (int i = 1; i < newIndicies.length; i++) {
        newIndicies[i] = i;
      }
      treeModel.nodesWereInserted(this, newIndicies);
    }
  }
}
