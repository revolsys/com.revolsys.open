package com.revolsys.swing.tree.model.node;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.springframework.util.StringUtils;

import com.revolsys.collection.IteratorEnumeration;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.ExceptionUtil;

public abstract class AbstractTreeNode implements TreeNode, Iterable<TreeNode> {

  public static void collapse(final TreeNode node) {
    collapseDescendents(node);
    if (node instanceof AbstractTreeNode) {
      final AbstractTreeNode treeNode = (AbstractTreeNode)node;
      final JTree tree = treeNode.getTree();
      final TreePath treePath = treeNode.getTreePath();
      tree.collapsePath(treePath);
    }
  }

  @SuppressWarnings("rawtypes")
  public static void collapseDescendents(final TreeNode node) {
    final Enumeration children = node.children();
    while (children.hasMoreElements()) {
      final Object child = children.nextElement();
      if (child instanceof TreeNode) {
        final TreeNode childNode = (TreeNode)child;
        collapse(childNode);
      }
    }
  }

  public static void expand(final TreeNode node) {
    if (node instanceof AbstractTreeNode) {
      final AbstractTreeNode treeNode = (AbstractTreeNode)node;
      final JTree tree = treeNode.getTree();
      final TreePath treePath = treeNode.getTreePath();
      tree.expandPath(treePath);
    }
  }

  @SuppressWarnings("rawtypes")
  public static void expandChildren(final TreeNode node) {
    expand(node);
    final Enumeration children = node.children();
    while (children.hasMoreElements()) {
      final Object child = children.nextElement();
      if (child instanceof TreeNode) {
        final TreeNode childNode = (TreeNode)child;
        expand(childNode);
      }
    }
  }

  public static TreePath getTreePath(final TreeNode node) {
    if (node == null) {
      return null;
    } else {
      final TreeNode parent = node.getParent();
      if (parent == null) {
        return new TreePath(node);
      } else {
        final TreePath parentPath = getTreePath(parent);
        return parentPath.pathByAddingChild(node);
      }
    }
  }

  private boolean allowsChildren;

  private Object userObject;

  private TreeNode parent;

  private String name;

  private Icon icon;

  private String type;

  private JTree tree;

  private int columnCount = 0;

  public AbstractTreeNode(final Object userObject) {
    this(userObject, false);
  }

  public AbstractTreeNode(final Object userObject, final boolean allowsChildren) {
    this(null, userObject, allowsChildren);
  }

  public AbstractTreeNode(final TreeNode parent, final Object userObject) {
    this(parent, userObject, false);
  }

  public AbstractTreeNode(final TreeNode parent, final Object userObject,
    final boolean allowsChildren) {
    this.parent = parent;
    this.userObject = userObject;
    this.allowsChildren = allowsChildren;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration children() {
    return IteratorEnumeration.create(getChildren());
  }

  public void collapse() {
    collapse(this);
  }

  public void collapseChildren() {
    collapseDescendents(this);
  }

  public final void delete() {
    try {
      final List<TreeNode> children = this.getChildren();
      delete(children);
      doDelete();
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), "Error deleting tree node: " + getName(), e);
    } finally {
      parent = null;
      name = "";
      type = "";
      tree = null;
      userObject = null;
    }
  }

  protected void delete(final List<TreeNode> children) {
    for (final TreeNode child : children) {
      try {
        if (child instanceof AbstractTreeNode) {
          final AbstractTreeNode treeNode = (AbstractTreeNode)child;
          treeNode.delete();
        }
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Error deleting tree node: " + child, e);
      }
    }
  }

  protected void doDelete() {
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object == null) {
      return false;
    } else if (object.getClass().equals(getClass())) {
      final AbstractTreeNode node = (AbstractTreeNode)object;
      return EqualsRegistry.equal(getUserObject(), node.getUserObject());
    } else {
      return false;
    }
  }

  public void expand() {
    expand(this);
  }

  public void expandChildren() {
    expandChildren(this);
  }

  @Override
  protected void finalize() throws Throwable {
    delete();
  }

  @Override
  public boolean getAllowsChildren() {
    return isAllowsChildren();
  }

  @Override
  public TreeNode getChildAt(final int index) {
    final List<TreeNode> children = getChildren();
    final TreeNode child = children.get(index);
    return child;
  }

  @Override
  public int getChildCount() {
    final List<TreeNode> children = getChildren();
    final int size = children.size();
    return size;
  }

  public List<TreeNode> getChildren() {
    return Collections.emptyList();
  }

  public int getColumnCount() {
    return columnCount;
  }

  public Icon getIcon() {
    return icon;
  }

  @Override
  public int getIndex(final TreeNode node) {
    final List<TreeNode> children = getChildren();
    return children.indexOf(node);
  }

  public MenuFactory getMenu() {
    return null;
  }

  public String getName() {
    return name;
  }

  @Override
  public TreeNode getParent() {
    return parent;
  }

  @SuppressWarnings("unchecked")
  public <V extends TreeNode> V getParentNode() {
    return (V)parent;
  }

  @SuppressWarnings("unchecked")
  public <V extends JTree> V getTree() {
    if (tree == null) {
      TreeNode parent = getParent();
      while (parent != null) {
        if (parent instanceof AbstractTreeNode) {
          final AbstractTreeNode treeNode = (AbstractTreeNode)parent;
          return (V)treeNode.getTree();
        }
        parent = parent.getParent();

      }
      return null;
    } else {
      return (V)tree;
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends TreeModel> V getTreeModel() {
    final JTree tree = getTree();
    return (V)tree.getModel();
  }

  public TreePath getTreePath() {
    return getTreePath(this);
  }

  public String getType() {
    return type;
  }

  @SuppressWarnings("unchecked")
  public <V> V getUserData() {
    return (V)userObject;
  }

  public Object getUserObject() {
    return userObject;
  }

  @Override
  public int hashCode() {
    final Object object = getUserObject();
    if (object == null) {
      return 1;
    } else {
      return object.hashCode();
    }
  }

  public boolean isAllowsChildren() {
    return allowsChildren;
  }

  @Override
  public boolean isLeaf() {
    return (getChildCount() == 0);
  }

  @Override
  public Iterator<TreeNode> iterator() {
    final List<TreeNode> children = getChildren();
    return children.iterator();
  }

  protected void nodeChanged() {
    final TreeModel model = getTreeModel();
    if (model instanceof DefaultTreeModel) {
      final DefaultTreeModel treeModel = (DefaultTreeModel)model;
      treeModel.nodeChanged(this);
    }
  }

  public void nodeCollapsed(final AbstractTreeNode treeNode) {
  }

  protected void nodeRemoved(final int index, final Object child) {
    nodesRemoved(new int[] {
      index
    }, child);
  }

  protected void nodesChanged(final int... indicies) {
    final TreeModel model = getTreeModel();
    if (model instanceof DefaultTreeModel) {
      final DefaultTreeModel treeModel = (DefaultTreeModel)model;
      treeModel.nodesChanged(this, indicies);
    }
  }

  protected void nodesInserted(final int... indicies) {
    final TreeModel model = getTreeModel();
    if (model instanceof DefaultTreeModel) {
      final DefaultTreeModel treeModel = (DefaultTreeModel)model;
      treeModel.nodesWereInserted(this, indicies);
    }
  }

  protected void nodesRemoved(final int[] indicies, final Object... children) {
    final TreeModel model = getTreeModel();
    if (model instanceof DefaultTreeModel) {
      final DefaultTreeModel treeModel = (DefaultTreeModel)model;
      treeModel.nodesWereRemoved(this, indicies, children);
    }
  }

  public void setAllowsChildren(final boolean allowsChildren) {
    this.allowsChildren = allowsChildren;
  }

  protected void setColumnCount(final int columnCount) {
    this.columnCount = columnCount;
  }

  protected void setIcon(final Icon icon) {
    this.icon = icon;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setParent(final TreeNode parent) {
    this.parent = parent;
  }

  public void setTree(final JTree tree) {
    this.tree = tree;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setUserObject(final Object userObject) {
    this.userObject = userObject;
  }

  @Override
  public String toString() {
    if (StringUtils.hasText(name)) {
      return name;
    } else {
      final Object userObject = getUserObject();
      if (userObject == null) {
        return "???";
      } else {
        return StringConverterRegistry.toString(userObject);
      }
    }
  }
}
