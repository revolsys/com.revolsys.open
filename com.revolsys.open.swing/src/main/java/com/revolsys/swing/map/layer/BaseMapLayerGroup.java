package com.revolsys.swing.map.layer;

import java.util.Map;

import com.revolsys.swing.menu.MenuFactory;

public class BaseMapLayerGroup extends LayerGroup {
  static {
    final MenuFactory menu = MenuFactory.getMenu(BaseMapLayerGroup.class);
    menu.addGroup(0, "group");
    menu.deleteMenuItem("group", "Add Group");
    menu.deleteMenuItem("group", "Open File Layer");
  }

  public static LayerGroup newLayer(final Map<String, Object> properties) {
    final BaseMapLayerGroup layerGroup = new BaseMapLayerGroup();
    layerGroup.loadLayers(properties);
    return layerGroup;
  }

  public BaseMapLayerGroup() {
    setType("baseMapLayerGroup");
    setOpen(false);
  }

  @Override
  public void addLayer(final int index, final Layer layer) {
    if (layer == null) {
    } else if (layer instanceof BaseMapLayer) {
      super.addLayer(index, layer);
    } else {
      throw new IllegalArgumentException("Layer " + layer.getName() + " must be a subclass of "
        + BaseMapLayer.class + " not " + layer.getClass());
    }
  }

  @Override
  public LayerGroup addLayerGroup() {
    return null;
  }

  @Override
  public String getName() {
    return "Base Maps";
  }

  @Override
  public boolean isSingleLayerVisible() {
    return true;
  }
}
