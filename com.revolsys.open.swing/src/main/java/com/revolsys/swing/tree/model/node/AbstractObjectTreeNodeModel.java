package com.revolsys.swing.tree.model.node;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.springframework.util.CollectionUtils;

import com.revolsys.beans.ClassUtil;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public abstract class AbstractObjectTreeNodeModel<NODE extends Object, CHILD extends Object>
  implements ObjectTreeNodeModel<NODE, CHILD> {
  private JPopupMenu menu = new JPopupMenu();

  private boolean lazyLoad = false;

  private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

  private Set<Class<?>> supportedClasses = new HashSet<Class<?>>();

  private Set<Class<?>> supportedChildClasses = new HashSet<Class<?>>();

  private List<ObjectTreeNodeModel<?, ?>> objectTreeNodeModels = new ArrayList<ObjectTreeNodeModel<?, ?>>();

  private MouseListener mouseListener;

  private boolean leaf;

  private ObjectTreeModel objectTreeModel;

  public AbstractObjectTreeNodeModel() {
  }

  public AbstractObjectTreeNodeModel(final ObjectTreeModel objectTreeModel) {
    this.objectTreeModel = objectTreeModel;
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
    return supportedChildClasses;
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

  protected JPopupMenu getMenu() {
    return menu;
  }

  @Override
  public JPopupMenu getMenu(final NODE node) {
    return menu;
  }

  @Override
  public MouseListener getMouseListener(final NODE node) {
    return mouseListener;
  }

  public ObjectTreeModel getObjectTreeModel() {
    return objectTreeModel;
  }

  @Override
  public ObjectTreeNodeModel<?, ?> getObjectTreeNodeModel(final Class<?> clazz) {
    final Set<Class<?>> classes = ClassUtil.getSuperClassesAndInterfaces(clazz);

    for (final ObjectTreeNodeModel<?, ?> objectTreeNodeModel : objectTreeNodeModels) {
      final Set<Class<?>> supportedClasses = objectTreeNodeModel.getSupportedClasses();
      if (CollectionUtils.containsAny(supportedClasses, classes)) {
        return objectTreeNodeModel;
      }
    }
    return null;
  }

  @Override
  public List<ObjectTreeNodeModel<?, ?>> getObjectTreeNodeModels() {
    return objectTreeNodeModels;
  }

  protected DefaultTreeCellRenderer getRenderer() {
    return renderer;
  }

  @Override
  public TreeCellRenderer getRenderer(final NODE node) {
    return renderer;
  }

  @Override
  public Set<Class<?>> getSupportedChildClasses() {
    return supportedChildClasses;
  }

  @Override
  public Set<Class<?>> getSupportedClasses() {
    return supportedClasses;
  }

  @Override
  public void initialize(final NODE node) {
  }

  @Override
  public boolean isLazyLoad() {
    return lazyLoad;
  }

  @Override
  public boolean isLeaf(final NODE node) {
    return leaf;
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

  protected void setMenu(final JPopupMenu menu) {
    this.menu = menu;
  }

  protected void setMouseListener(final MouseListener mouseListener) {
    this.mouseListener = mouseListener;
  }

  @Override
  public void setObjectTreeModel(final ObjectTreeModel objectTreeModel) {
    this.objectTreeModel = objectTreeModel;
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
