package com.revolsys.swing.map.layer.mapguide;

import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.record.io.format.esri.rest.map.FeatureLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisRestServerRecordLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;

public class MapGuideWebServerRecordLayer {
  private static final String J_TYPE = "mapGuideWebServerRecordLayer";

  private static void actionAddLayer(final FeatureLayer layerDescription) {
    final Project project = Project.get();
    if (project != null) {

      LayerGroup layerGroup = project;
      final PathName layerPath = layerDescription.getPathName();
      for (final String groupName : layerPath.getParent().getElements()) {
        layerGroup = layerGroup.addLayerGroup(groupName);
      }
      // final ArcGisRestServerRecordLayer layer = new
      // ArcGisRestServerRecordLayer(layerDescription);
      // layerGroup.addLayer(layer);
      // if (OS.getPreferenceBoolean("com.revolsys.gis",
      // AbstractLayer.PREFERENCE_PATH,
      // AbstractLayer.PREFERENCE_NEW_LAYERS_SHOW_TABLE_VIEW, false)) {
      // layer.showTableView();
      // }
    }
  }

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "Map Guide Web Server Record Layer",
      ArcGisRestServerRecordLayer::new);

    final MenuFactory recordLayerDescriptionMenu = MenuFactory.getMenu(FeatureLayer.class);

    Menus.addMenuItem(recordLayerDescriptionMenu, "default", "Add Layer", "map_add",
      MapGuideWebServerRecordLayer::actionAddLayer);
  }
}
