package com.revolsys.swing.map.layer;

import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.swing.menu.MenuFactory;

public class BaseMapLayerGroup extends LayerGroup {
  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "baseMapLayerGroup", "Base Map Layer Group", BaseMapLayerGroup.class,
      "create");

  static {
    final MenuFactory menu = MenuFactory.getMenu(BaseMapLayerGroup.class);
    menu.addGroup(0, "group");
    menu.deleteMenuItem("group", "Add Group");
    menu.deleteMenuItem("group", "Open File Layer");
  }

  public BaseMapLayerGroup() {
    setType("baseMapLayerGroup");
  }

  @Override
  public void addLayer(final int index, final Layer layer) {
    if (layer == null) {
    } else if (layer instanceof BaseMapLayer) {
      super.addLayer(index, layer);
    } else {
      throw new IllegalArgumentException("Layer " + layer.getName()
        + " must be a subclass of " + BaseMapLayer.class + " not "
        + layer.getClass());
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
}
