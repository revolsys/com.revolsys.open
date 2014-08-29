package com.revolsys.swing.tree.node;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.revolsys.collection.IteratorEnumeration;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeNodePropertyEnableCheck;
import com.revolsys.swing.tree.dnd.TreePathListTransferable;
import com.revolsys.swing.tree.dnd.TreeTransferHandler;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class BaseTreeNode implements TreeNode, Iterable<BaseTreeNode>,
  PropertyChangeListener {

  @SuppressWarnings("unchecked")
  public static <V> V getUserData(final TreePath path) {
    Object value = path.getLastPathComponent();
    if (value instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)value;
      value = node.getUserData();
    }
    return (V)value;
  }

  private boolean visible = true;

  public static final EnableCheck NODE_EXISTS = new TreeNodePropertyEnableCheck(
    "exists");

  private boolean allowsChildren;

  private Object userObject;

  private Reference<BaseTreeNode> parent;

  private String name;

  private Icon icon;

  private String type;

  private JTree tree;

  private boolean userObjectInitialized = true;

  public BaseTreeNode(final boolean allowsChildren) {
    this(null, allowsChildren);
  }

  public BaseTreeNode(final Object userObject) {
    this(userObject, false);
  }

  public BaseTreeNode(final Object userObject, final boolean allowsChildren) {
    this.userObject = userObject;
    this.allowsChildren = allowsChildren;
  }

  protected int addChild(final int index, final Object child) {
    throw new UnsupportedOperationException();
  }

  protected int addChild(final Object child) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration children() {
    return IteratorEnumeration.create(getChildren());
  }

  public void collapse() {
    collapseChildren();

    final JTree tree = getTree();
    if (tree != null) {
      final TreePath treePath = getTreePath();
      if (treePath != null) {
        tree.collapsePath(treePath);
      }
    }
  }

  public void collapseChildren() {
    for (final BaseTreeNode child : getChildren()) {
      child.collapse();
    }
  }

  public void delete() {
    try {
      final List<BaseTreeNode> children = this.getChildren();
      delete(children);
      doClose();
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), "Error deleting tree node: " + getName(), e);
    } finally {
      setParent(null);
      this.name = "";
      this.type = "";
      this.tree = null;
      this.userObject = null;
    }
  }

  protected void delete(final List<BaseTreeNode> children) {
    for (final BaseTreeNode child : children) {
      try {
        child.delete();
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Error deleting tree node: " + child, e);
      }
    }
  }

  public boolean dndImportData(final TransferSupport support, int index)
    throws IOException, UnsupportedFlavorException {
    if (!TreeTransferHandler.isDndNoneAction(support)) {
      final Transferable transferable = support.getTransferable();
      if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
        final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
        if (data instanceof TreePathListTransferable) {
          final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
          final List<TreePath> pathList = pathTransferable.getPaths();
          for (final TreePath sourcePath : pathList) {
            index = importData(support, pathTransferable, index, sourcePath);
          }
        }
        return true;
      }
    }
    return false;
  }

  protected void doClose() {
  }

  protected void doPropertyChange(final PropertyChangeEvent e) {
    if (e.getSource() == getUserObject()) {
      final TreeModel model = getTreeModel();
      if (model instanceof DefaultTreeModel) {
        final DefaultTreeModel defaultModel = (DefaultTreeModel)model;
        defaultModel.nodeChanged(this);
      }
    }
  }

  @Override
  public boolean equals(final Object object) {
    final Object userObject = getUserObject();
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object == userObject) {
      return true;
    } else if (object.getClass().equals(getClass())) {
      final BaseTreeNode node = (BaseTreeNode)object;
      final Object otherUserObject1 = node.getUserObject();
      if (EqualsRegistry.equal(userObject, otherUserObject1)) {
        return true;
      } else {
        return false;
      }
    } else {
      if (userObject != null && userObject.equals(object)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public void expand() {
    final TreePath treePath = getTreePath();
    expand(treePath);
  }

  public void expand(final List<?> path) {
    if (path != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final TreePath treePath = getTreePath(path);
        expand(treePath);
      } else {
        Invoke.andWait(this, "expand", path);
      }
    }
  }

  public void expand(final TreePath treePath) {
    final JTree tree = getTree();
    if (tree != null) {
      tree.expandPath(treePath);
    }
  }

  public void expandChildren() {
    expand();
    for (final BaseTreeNode child : getChildren()) {
      child.expand();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    delete();
  }

  @Override
  public boolean getAllowsChildren() {
    return isAllowsChildren();
  }

  public BaseTreeNode getChild(final Object item) {
    for (final BaseTreeNode child : getChildren()) {
      if (child.equals(item)) {
        return child;
      }
    }
    return null;
  }

  @Override
  public BaseTreeNode getChildAt(final int index) {
    final List<BaseTreeNode> children = getChildren();
    final BaseTreeNode child = children.get(index);
    return child;
  }

  protected List<Class<?>> getChildClasses() {
    return Collections.emptyList();
  }

  @Override
  public int getChildCount() {
    final List<BaseTreeNode> children = getChildren();
    final int size = children.size();
    return size;
  }

  public List<BaseTreeNode> getChildren() {
    return Collections.emptyList();
  }

  public Icon getIcon() {
    return this.icon;
  }

  @Override
  public int getIndex(final TreeNode node) {
    final List<BaseTreeNode> children = getChildren();
    return children.indexOf(node);
  }

  protected int getIndexOfChild(final BaseTreeNode abstractTreeNode,
    final Object child) {
    if (child != null) {
      final List<BaseTreeNode> children = getChildren();
      for (int i = 0; i < children.size(); i++) {
        final BaseTreeNode childNode = children.get(i);
        if (childNode instanceof BaseTreeNode) {
          final BaseTreeNode node = childNode;
          if (child.equals(node.getUserObject())) {
            return i;
          }
        }
      }
    }
    return -1;
  }

  public MenuFactory getMenu() {
    final Object object = getUserObject();
    if (object == null) {
      return null;
    } else {
      final Class<?> clazz = object.getClass();
      return MenuFactory.getMenu(clazz);
    }
  }

  public String getName() {
    return this.name;
  }

  @Override
  public BaseTreeNode getParent() {
    if (this.parent == null) {
      return null;
    } else {
      return this.parent.get();
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends BaseTreeNode> V getParentNode() {
    return (V)getParent();
  }

  @SuppressWarnings("unchecked")
  public <V extends JTree> V getTree() {
    if (this.tree == null) {
      final BaseTreeNode parent = getParent();
      if (parent == null) {
        return null;
      } else {
        return parent.getTree();
      }
    } else {
      return (V)this.tree;
    }
  }

  public Component getTreeCellRendererComponent(final Component renderer,
    final JTree tree, final Object value, final boolean selected,
    final boolean expanded, final boolean leaf, final int row,
    final boolean hasFocus) {
    final Icon icon = getIcon();
    if (icon != null && renderer instanceof JLabel) {
      ((JLabel)renderer).setIcon(icon);
    }
    return renderer;
  }

  @SuppressWarnings("unchecked")
  public <V extends TreeModel> V getTreeModel() {
    final JTree tree = getTree();
    if (tree == null) {
      return null;
    } else {
      return (V)tree.getModel();
    }
  }

  public TreePath getTreePath() {
    final BaseTreeNode parent = getParent();
    if (parent == null) {
      return new TreePath(this);
    } else {
      final TreePath parentPath = parent.getTreePath();
      return parentPath.pathByAddingChild(this);
    }
  }

  public TreePath getTreePath(final List<?> path) {
    if (path != null && !path.isEmpty()) {
      final Object first = path.get(0);
      if (equals(first)) {
        final List<?> subPath = path.subList(1, path.size());
        if (subPath.isEmpty()) {
          return getTreePath();
        } else {
          final Object child = subPath.get(0);
          final BaseTreeNode childNode = getChild(child);
          if (childNode != null) {
            return childNode.getTreePath(subPath);
          }
        }
      }
    }
    return null;
  }

  public TreePath getTreePath(final Object item) {
    final BaseTreeNode child = getChild(item);
    if (child == null) {
      return null;
    } else {
      return child.getTreePath();
    }
  }

  public String getType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  public <V> V getUserData() {
    return (V)this.userObject;
  }

  public Object getUserObject() {
    return this.userObject;
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

  protected int importData(final TransferSupport support,
    final TreePathListTransferable pathTransferable, int index,
    final TreePath sourcePath) {
    Object child = getUserData(sourcePath);
    if (TreeTransferHandler.isDndCopyAction(support)) {
      if (isCopySupported(child)) {
        child = JavaBeanUtil.clone(child);
        pathTransferable.addCopiedPath(sourcePath);
      } else {
        return index;
      }
    } else {
      final int childIndex = getIndexOfChild(this, child);
      if (childIndex > -1) {
        removeChild(child);
        pathTransferable.setSameParent(sourcePath);
        if (index != -1) {
          if (childIndex > -1 && childIndex < index) {
            index--;
          }
        }
      } else {
        pathTransferable.addMovedPath(sourcePath);
      }
    }
    if (index != -1) {
      addChild(index, child);
      index++;
    } else {
      addChild(child);
    }
    return index;
  }

  public boolean isAllowsChildren() {
    return this.allowsChildren;
  }

  public boolean isChildrenInitialized() {
    return true;
  }

  public boolean isCopySupported() {
    return false;
  }

  public boolean isCopySupported(final Object child) {
    return child instanceof Cloneable;
  }

  public boolean isDndCanImport(final TreePath dropPath,
    final TransferSupport support) {
    if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
      try {
        final Object node = dropPath.getLastPathComponent();
        if (node == this) {
          final Transferable transferable = support.getTransferable();
          final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
          if (data instanceof TreePathListTransferable) {
            final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
            final List<TreePath> pathList = pathTransferable.getPaths();
            for (final TreePath treePath : pathList) {
              if (!isDndDropSupported(support, dropPath, treePath)) {
                return false;
              }
            }
          }
          support.setShowDropLocation(true);
          return true;
        }
      } catch (final Exception e) {
        return false;
      }
    }
    return false;
  }

  public boolean isDndDropSupported(final TransferSupport support,
    final TreePath dropPath, final TreePath sourcePath) {
    final boolean descendant = sourcePath.isDescendant(dropPath);
    if (!descendant) {
      final Object value = getUserData(sourcePath);
      if (isDndDropSupported(support, dropPath, sourcePath, value)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isDndDropSupported(final TransferSupport support,
    final TreePath dropPath, final TreePath childPath, final Object child) {
    final Class<?> valueClass = child.getClass();
    final List<Class<?>> childClasses = getChildClasses();
    for (final Class<?> supportedClass : childClasses) {
      if (supportedClass.isAssignableFrom(valueClass)) {
        if (TreeTransferHandler.isDndCopyAction(support)) {
          if (!isCopySupported(child)) {
            return false;
          }
        }

        return true;
      }
    }
    return false;
  }

  public boolean isExists() {
    final BaseTreeNode parent = getParent();
    if (parent == null) {
      return true;
    } else {
      return parent.isExists();
    }
  }

  @Override
  public boolean isLeaf() {
    return getChildCount() == 0;
  }

  public boolean isUserObjectInitialized() {
    return this.userObjectInitialized;
  }

  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public Iterator<BaseTreeNode> iterator() {
    final List<BaseTreeNode> children = getChildren();
    return children.iterator();
  }

  public void mouseClicked(final MouseEvent e) {
  }

  public void mouseEntered(final MouseEvent e) {
  }

  public void mouseExited(final MouseEvent e) {
  }

  public void mousePressed(final MouseEvent e) {
  }

  public void mouseReleased(final MouseEvent e) {
  }

  protected void nodeChanged() {
    final TreeModel model = getTreeModel();
    if (model instanceof DefaultTreeModel) {
      final DefaultTreeModel treeModel = (DefaultTreeModel)model;
      treeModel.nodeChanged(this);
    }
  }

  public void nodeCollapsed(final BaseTreeNode treeNode) {
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

  @Override
  public final void propertyChange(final PropertyChangeEvent e) {
    if (SwingUtilities.isEventDispatchThread()) {
      doPropertyChange(e);
    } else {
      Invoke.later(this, "propertyChange", e);
    }
  }

  public boolean removeChild(final Object child) {
    throw new UnsupportedOperationException();
  }

  public boolean removeChild(final TreePath path) {
    final Object child = getUserData(path);
    return removeChild(child);
  }

  public void setAllowsChildren(final boolean allowsChildren) {
    this.allowsChildren = allowsChildren;
  }

  protected void setIcon(final Icon icon) {
    this.icon = icon;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setParent(final BaseTreeNode parent) {
    final Object userObject = getUserObject();
    if (parent == null) {
      this.parent = null;
      Property.removeListener(userObject, this);
    } else {
      this.parent = new WeakReference<>(parent);
      Property.addListener(userObject, this);
    }
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

  public void setUserObjectInitialized(final boolean userObjectInitialized) {
    this.userObjectInitialized = userObjectInitialized;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
    nodeChanged();
  }

  @Override
  public String toString() {
    if (Property.hasValue(this.name)) {
      return this.name;
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
