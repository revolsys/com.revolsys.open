package com.revolsys.swing.tree.node.layer;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.swing.tree.node.OpenStateTreeNode;

public class LayerRendererTreeNode extends ListTreeNode
  implements MouseListener, OpenStateTreeNode {
  public LayerRendererTreeNode(final LayerRenderer<?> renderer) {
    super(renderer);
    setName(renderer.getName());
    setIcon(renderer.getIcon());
    if (renderer instanceof MultipleLayerRenderer) {
      setAllowsChildren(true);
    } else {
      setAllowsChildren(false);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public int addChild(final int index, final Object object) {
    final MultipleLayerRenderer<Layer, LayerRenderer<Layer>> renderer = getMutipleRenderer();
    if (renderer != null && renderer.canAddChild(object)) {
      final LayerRenderer<Layer> child = (LayerRenderer<Layer>)object;
      renderer.addRenderer(index, child);
      return index;
    }

    return -1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int addChild(final Object object) {
    final MultipleLayerRenderer<Layer, LayerRenderer<Layer>> renderer = getMutipleRenderer();
    if (renderer != null && renderer.canAddChild(object)) {
      final LayerRenderer<Layer> child = (LayerRenderer<Layer>)object;
      renderer.addRenderer(child);
      return getChildCount();
    }

    return -1;
  }

  @Override
  protected List<Class<?>> getChildClasses() {
    return Arrays.<Class<?>> asList(AbstractLayerRenderer.class);
  }

  @Override
  public Icon getIcon() {
    final Icon icon = getRenderer().getIcon();
    if (icon != super.getIcon()) {
      setIcon(icon);
    }
    return icon;
  }

  @SuppressWarnings("unchecked")
  public MultipleLayerRenderer<Layer, LayerRenderer<Layer>> getMutipleRenderer() {
    final LayerRenderer<?> renderer = getRenderer();
    if (renderer instanceof MultipleLayerRenderer) {
      return (MultipleLayerRenderer<Layer, LayerRenderer<Layer>>)renderer;
    } else {
      return null;
    }

  }

  public <V extends LayerRenderer<?>> V getRenderer() {
    return getUserData();
  }

  @Override
  public Component getTreeCellRendererComponent(Component renderer, final JTree tree,
    final Object value, final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
    renderer = super.getTreeCellRendererComponent(renderer, tree, value, selected, expanded, leaf,
      row, hasFocus);
    renderer.setEnabled(getRenderer().isVisible());
    return renderer;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected boolean isDndDropSupported(final TransferSupport support, final TreePath dropPath,
    final TreePath childPath, final Object child) {
    final MultipleLayerRenderer<Layer, LayerRenderer<Layer>> mutipleRenderer = getMutipleRenderer();
    if (mutipleRenderer != null && mutipleRenderer.canAddChild(child)) {
      if (super.isDndDropSupported(support, dropPath, childPath, child)) {
        final LayerRenderer<Layer> childRenderer = (LayerRenderer<Layer>)child;
        if (!mutipleRenderer.isSameLayer(childRenderer)) {
          if (isCopySupported(childRenderer)) {
            support.setDropAction(DnDConstants.ACTION_COPY);
          } else {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isOpen() {
    final LayerRenderer<?> renderer = getRenderer();
    return renderer.isOpen();
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final MultipleLayerRenderer<Layer, LayerRenderer<Layer>> mutipleRenderer = getMutipleRenderer();

    if (mutipleRenderer == null) {
      return Collections.emptyList();
    } else {
      final List<BaseTreeNode> nodes = new ArrayList<>();
      for (final LayerRenderer<?> childRenderer : mutipleRenderer.getRenderers()) {
        final LayerRendererTreeNode node = new LayerRendererTreeNode(childRenderer);
        nodes.add(node);
      }
      return nodes;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    final JTree tree = getTree();
    if (source == tree) {
      final int clickCount = e.getClickCount();
      if (clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
        final int x = e.getX();
        final int y = e.getY();
        final TreePath path = tree.getPathForLocation(x, y);
        final LayerRenderer<?> renderer = getRenderer();
        final TreeUI ui = tree.getUI();
        final Rectangle bounds = ui.getPathBounds(tree, path);
        final int cX = x - bounds.x;
        final int index = cX / 21;
        int offset = 0;
        if (index == offset) {
          renderer.setVisible(!renderer.isVisible());
        }
        offset++;
        e.consume();
      }
    }
  }

  @Override
  protected void propertyChangeDo(final PropertyChangeEvent e) {
    final Object source = e.getSource();
    final LayerRenderer<?> renderer = getRenderer();
    if (source == renderer) {
      final String propertyName = e.getPropertyName();
      if (propertyName.equals("renderers")) {
        refresh();
      } else if ("name".equals(propertyName)) {
        setName((String)e.getNewValue());
      } else if ("icon".equals(propertyName)) {
        setIcon((Icon)e.getNewValue());
      }
    }
    super.propertyChangeDo(e);
    nodeChanged();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean removeChild(final Object object) {
    final MultipleLayerRenderer<Layer, LayerRenderer<Layer>> renderer = getMutipleRenderer();
    if (renderer != null && renderer.canAddChild(object)) {
      final LayerRenderer<Layer> child = (LayerRenderer<Layer>)object;
      return renderer.removeRenderer(child) != -1;
    }
    return false;
  }

  @Override
  public void setOpen(final boolean open) {
    final LayerRenderer<?> renderer = getRenderer();
    renderer.setOpen(open);
  }
}
