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
      return getParent().addLayer(layer);
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
    importBaseMaps.forEach(this::addLayer);
  }

  @Override
  public boolean isSingleLayerVisible() {
    return true;
  }
}
