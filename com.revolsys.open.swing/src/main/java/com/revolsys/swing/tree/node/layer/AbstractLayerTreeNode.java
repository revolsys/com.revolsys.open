package com.revolsys.swing.tree.node.layer;

import java.awt.Component;
import java.beans.PropertyChangeEvent;

import javax.swing.JTree;

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.swing.tree.node.OpenStateTreeNode;

public abstract class AbstractLayerTreeNode extends ListTreeNode implements OpenStateTreeNode {

  public AbstractLayerTreeNode(final Layer layer) {
    super(layer);
    setName(layer.getName());
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }

  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer() {
    return (V)getUserData();
  }

  @Override
  public MenuFactory getMenu() {
    final Layer layer = getLayer();
    if (layer == null) {
      return super.getMenu();
    } else {
      return layer.getMenu();
    }
  }

  @Override
  public LayerGroupTreeNode getParent() {
    return (LayerGroupTreeNode)super.getParent();
  }

  @Override
  public Component getTreeCellRendererComponent(Component renderer, final JTree tree,
    final Object value, final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
    renderer = super.getTreeCellRendererComponent(renderer, tree, value, selected, expanded, leaf,
      row, hasFocus);
    if (isUserObjectInitialized()) {
      final Layer layer = getLayer();

      final MapPanel map = layer.getMapPanel();
      if (map != null) {
        final double scale = map.getScale();
        if (layer.getRenderer() != null && !layer.isVisible(scale)) {
          if (!selected) {
            renderer.setForeground(WebColors.Gray);
          }
        }
      }
    }
    return renderer;
  }

  @Override
  public boolean isCopySupported() {
    final Layer layer = getLayer();
    return layer.isClonable();
  }

  @Override
  public boolean isOpen() {
    final Layer layer = getLayer();
    return layer.isOpen();
  }

  @Override
  public boolean isUserObjectInitialized() {
    return getLayer().isInitialized();
  }

  @Override
  protected void propertyChangeDo(final PropertyChangeEvent e) {
    super.propertyChangeDo(e);
    final Object source = e.getSource();
    if (source == getLayer()) {
      final String propertyName = e.getPropertyName();
      if ("name".equals(propertyName)) {
        Invoke.later(() -> {
          setName(getLayer().getName());
          nodeChanged();
        });
      }
    }
  }

  @Override
  public void setOpen(final boolean open) {
    final Layer layer = getLayer();
    layer.setOpen(open);
  }
}
