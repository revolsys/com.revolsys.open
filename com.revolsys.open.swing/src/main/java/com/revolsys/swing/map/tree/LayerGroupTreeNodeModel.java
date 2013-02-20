package com.revolsys.swing.map.tree;

import java.util.List;

import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.tree.renderer.LayerGroupTreeCellRenderer;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class LayerGroupTreeNodeModel extends
  AbstractObjectTreeNodeModel<LayerGroup, Layer> {

  public LayerGroupTreeNodeModel() {
    setSupportedClasses(LayerGroup.class);
    setSupportedChildClasses(AbstractLayer.class, LayerGroup.class, Layer.class);
    setObjectTreeNodeModels(this, new BaseLayerTreeNodeModel("Layer"));
    setRenderer(new LayerGroupTreeCellRenderer());
  }

  @Override
  public int addChild(final LayerGroup parent, final int index,
    final Layer layer) {
    parent.add(index, layer);
    return index;
  }

  @Override
  public int addChild(final LayerGroup parent, final Layer layer) {
    parent.add(layer);
    return getChildCount(parent);
  }

  @Override
  protected List<Layer> getChildren(final LayerGroup parent) {
    return parent.getLayers();
  }

  @Override
  public boolean removeChild(final LayerGroup parent, final Layer layer) {
    parent.remove(layer);
    return true;
  }
}
