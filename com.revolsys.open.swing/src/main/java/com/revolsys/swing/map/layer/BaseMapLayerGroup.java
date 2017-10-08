package com.revolsys.swing.map.layer;

import java.util.Map;

import com.revolsys.swing.menu.MenuFactory;

public class BaseMapLayerGroup extends LayerGroup {
  static {
    MenuFactory.addMenuInitializer(BaseMapLayerGroup.class, (menu) -> {
      menu.deleteGroup("scale");
      menu.deleteMenuItem("zoom", "Zoom to Layer");
      menu.deleteMenuItem("group", "Add Group");
      menu.deleteMenuItem("group", "Open File Layer...");
      menu.deleteMenuItem("layer", "Delete");
      menu.deleteMenuItem("layer", "Layer Properties");
    });
  }

  public static LayerGroup newLayer(final Map<String, ? extends Object> properties) {
    final BaseMapLayerGroup layerGroup = new BaseMapLayerGroup();
    final Project project = layerGroup.getProject();
    layerGroup.loadLayers(project, properties);
    return layerGroup;
  }

  public BaseMapLayerGroup() {
    setType("baseMapLayerGroup");
    setOpen(false);
  }

  @Override
  public boolean addLayer(final int index, final Layer layer) {
    if (layer == null) {
      return false;
    } else if (layer instanceof BaseMapLayer) {
      return super.addLayer(index, layer);
    } else {
      throw new IllegalArgumentException("Layer " + layer.getName() + " must be a subclass of "
        + BaseMapLayer.class + " not " + layer.getClass());
    }
  }

  @Override
  public String getName() {
    return "Base Maps";
  }

  @Override
  protected void importProject(final Project importProject) {
    importProjectBaseMaps(importProject);
  }

  protected void importProjectBaseMaps(final Project importProject) {
    final BaseMapLayerGroup importBaseMaps = importProject.getBaseMapLayers();
    addLayers(importBaseMaps);
  }

  @Override
  public boolean isSingleLayerVisible() {
    return true;
  }
}
