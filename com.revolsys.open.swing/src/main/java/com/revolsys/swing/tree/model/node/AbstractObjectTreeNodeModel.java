package com.revolsys.swing.tree.model.node;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.springframework.util.CollectionUtils;

import com.revolsys.beans.ClassUtil;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.dnd.transferable.TreePathListTransferable;
import com.revolsys.swing.dnd.transferhandler.ObjectTreeTransferHandler;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public abstract class AbstractObjectTreeNodeModel<NODE extends Object, CHILD extends Object>
  implements ObjectTreeNodeModel<NODE, CHILD> {

  public static final ImageIcon ICON_FOLDER = SilkIconLoader.getIcon("folder");

  private boolean lazyLoad = false;

  private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

  private Set<Class<?>> supportedClasses = new HashSet<Class<?>>();

  private Set<Class<?>> supportedChildClasses = new HashSet<Class<?>>();

  private List<ObjectTreeNodeModel<?, ?>> objectTreeNodeModels = new ArrayList<ObjectTreeNodeModel<?, ?>>();

  private MouseListener mouseListener;

  private boolean leaf;

  private ObjectTreeModel objectTreeModel;

  public AbstractObjectTreeNodeModel() {
    this(null);
  }

  public AbstractObjectTreeNodeModel(final ObjectTreeModel objectTreeModel) {
    this.objectTreeModel = objectTreeModel;
    this.renderer.setClosedIcon(ICON_FOLDER);
    this.renderer.setOpenIcon(ICON_FOLDER);
  }

  @Override
  public int addChild(final NODE node, final CHILD child) {
    return -1;
  }

  @Override
  public int addChild(final NODE node, final int index, final CHILD child) {
    return -1;
  }

  protected void addObjectTreeNodeModels(
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    this.objectTreeNodeModels.addAll(Arrays.asList(objectTreeNodeModels));
    if (this.objectTreeModel != null) {
      for (final ObjectTreeNodeModel<?, ?> nodeModel : objectTreeNodeModels) {
        nodeModel.setObjectTreeModel(this.objectTreeModel);
      }
    }
  }

  @Override
  public boolean canImport(final TreePath path, final TransferSupport support) {
    if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
      final Set<Class<?>> supportedClasses = getSupportedChildClasses();
      try {
        final Transferable transferable = support.getTransferable();
        final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
        if (data instanceof TreePathListTransferable) {
          final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
          final List<TreePath> pathList = pathTransferable.getPaths();
          for (final TreePath treePath : pathList) {
            if (!ObjectTreeTransferHandler.isDropSupported(treePath,
              supportedClasses)) {
              return false;
            }
          }
        }
        support.setShowDropLocation(true);
        return true;
      } catch (final Exception e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public String convertValueToText(final NODE node, final boolean selected,
    final boolean expanded, final boolean leaf, final int row,
    final boolean hasFocus) {
    if (node == null) {
      return "";
    } else {
      return node.toString();
    }
  }

  @Override
  public CHILD getChild(final NODE node, final int index) {
    final List<CHILD> children = getChildren(node);
    if (index < children.size()) {
      return children.get(index);
    } else {
      return null;
    }
  }

  protected Set<Class<?>> getChildClasses() {
    return this.supportedChildClasses;
  }

  @Override
  public int getChildCount(final NODE node) {
    final List<CHILD> children = getChildren(node);
    return children.size();
  }

  protected List<CHILD> getChildren(final NODE node) {
    return Collections.emptyList();
  }

  @Override
  public int getIndexOfChild(final NODE node, final CHILD child) {
    final List<CHILD> children = getChildren(node);
    return children.indexOf(child);
  }

  @Override
  public Object getLabel(final NODE node) {
    return node;
  }

  @Override
  public MenuFactory getMenu(final NODE node) {
    if (node == null || this.objectTreeModel == null) {
      return null;
    } else {
      return this.objectTreeModel.getMenu(node);
    }
  }

  @Override
  public MouseListener getMouseListener(final NODE node) {
    return this.mouseListener;
  }

  public ObjectTreeModel getObjectTreeModel() {
    return this.objectTreeModel;
  }

  @Override
  public ObjectTreeNodeModel<?, ?> getObjectTreeNodeModel(final Class<?> clazz) {
    final Set<Class<?>> classes = ClassUtil.getSuperClassesAndInterfaces(clazz);

    for (final ObjectTreeNodeModel<?, ?> objectTreeNodeModel : this.objectTreeNodeModels) {
      final Set<Class<?>> supportedClasses = objectTreeNodeModel.getSupportedClasses();
      if (CollectionUtils.containsAny(supportedClasses, classes)) {
        return objectTreeNodeModel;
      }
    }
    return null;
  }

  @Override
  public List<ObjectTreeNodeModel<?, ?>> getObjectTreeNodeModels() {
    return this.objectTreeNodeModels;
  }

  @Override
  public <T> T getParent(final NODE node) {
    return null;
  }

  protected DefaultTreeCellRenderer getRenderer() {
    return this.renderer;
  }

  @Override
  public Component getRenderer(final NODE node, final JTree tree,
    final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
    final Object label = getLabel(node);

    return this.renderer.getTreeCellRendererComponent(tree, label, selected,
      expanded, leaf, row, hasFocus);
  }

  @Override
  public Set<Class<?>> getSupportedChildClasses() {
    return this.supportedChildClasses;
  }

  @Override
  public Set<Class<?>> getSupportedClasses() {
    return this.supportedClasses;
  }

  @Override
  public void initialize(final NODE node) {
  }

  @Override
  public boolean isLazyLoad() {
    return this.lazyLoad;
  }

  @Override
  public boolean isLeaf(final NODE node) {
    return this.leaf;
  }

  @Override
  public boolean removeChild(final NODE node, final CHILD child) {
    return false;
  }

  protected void setLazyLoad(final boolean lazyLoad) {
    this.lazyLoad = lazyLoad;
  }

  protected void setLeaf(final boolean leaf) {
    this.leaf = leaf;
  }

  protected void setMouseListener(final MouseListener mouseListener) {
    this.mouseListener = mouseListener;
  }

  @Override
  public void setObjectTreeModel(final ObjectTreeModel objectTreeModel) {
    if (this.objectTreeModel != objectTreeModel) {
      this.objectTreeModel = objectTreeModel;
      for (final ObjectTreeNodeModel<?, ?> nodeModel : this.objectTreeNodeModels) {
        nodeModel.setObjectTreeModel(objectTreeModel);
      }
    }
  }

  protected void setObjectTreeNodeModels(
    final List<ObjectTreeNodeModel<?, ?>> objectTreeNodeModels) {
    this.objectTreeNodeModels = objectTreeNodeModels;
  }

  protected void setObjectTreeNodeModels(
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    setObjectTreeNodeModels(Arrays.asList(objectTreeNodeModels));
  }

  protected void setRenderer(final DefaultTreeCellRenderer renderer) {
    this.renderer = renderer;
  }

  protected void setSupportedChildClasses(
    final Class<?>... supportedChildClasses) {
    final List<Class<?>> list = Arrays.asList(supportedChildClasses);
    this.supportedChildClasses.addAll(list);
  }

  protected void setSupportedChildClasses(
    final Set<Class<?>> supportedChildClasses) {
    this.supportedChildClasses = supportedChildClasses;
  }

  protected void setSupportedClasses(final Class<?>... supportedClasses) {
    final List<Class<?>> list = Arrays.asList(supportedClasses);
    this.supportedClasses.addAll(list);
  }

  protected void setSupportedClasses(final Set<Class<?>> supportedClasses) {
    this.supportedClasses = supportedClasses;
  }
}
