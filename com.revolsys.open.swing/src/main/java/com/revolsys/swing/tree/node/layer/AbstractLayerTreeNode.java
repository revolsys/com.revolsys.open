package com.revolsys.swing.tree.node.layer;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.tree.node.ListTreeNode;

public abstract class AbstractLayerTreeNode extends ListTreeNode {

  public AbstractLayerTreeNode(final Layer layer) {
    super(layer);
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
  public boolean isCopySupported() {
    final Layer layer = getLayer();
    return layer.isClonable();
  }

  @Override
  public boolean isEnabled() {
    return getLayer().isInitialized();
  }

}
