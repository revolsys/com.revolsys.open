package com.revolsys.swing.tree.node.layer;

import java.awt.Component;
import java.beans.PropertyChangeEvent;

import javax.swing.JTree;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.swing.tree.node.OpenStateTreeNode;

public abstract class AbstractLayerTreeNode extends ListTreeNode implements OpenStateTreeNode {

  public AbstractLayerTreeNode(final Layer layer) {
    super(layer);
  }

  @Override
  protected void doPropertyChange(final PropertyChangeEvent e) {
    super.doPropertyChange(e);
    final Object source = e.getSource();
    if (source == getLayer()) {
      final String propertyName = e.getPropertyName();
      if ("name".equals(propertyName)) {
        setName(getLayer().getName());
        nodeChanged();
      }
    }
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

      final MapPanel map = MapPanel.get(layer);
      if (map != null) {
        final double scale = map.getScale();
        if (layer.getRenderer() != null && !layer.isVisible(scale)) {
          renderer.setForeground(WebColors.Gray);
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
  public void setOpen(final boolean open) {
    final Layer layer = getLayer();
    layer.setOpen(open);
  }
}
