package com.revolsys.swing.map.layer.mapguide;

import org.jeometry.common.io.PathName;

import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.record.io.format.mapguide.MapGuideFeatureLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;

public class MapGuideWebServer {
  private static final String J_TYPE = "mapGuideWebServerRecordLayer";

  private static void actionAddLayer(final MapGuideFeatureLayer layerDescription) {
    final Project project = Project.get();
    if (project != null) {

      LayerGroup layerGroup = project;
      final PathName layerPath = layerDescription.getPathName();
      for (final String groupName : layerPath.getParent().getElements()) {
        layerGroup = layerGroup.addLayerGroup(groupName);
      }
      final MapGuideWebServerRecordLayer layer = new MapGuideWebServerRecordLayer(layerDescription);
      layerGroup.addLayer(layer);
      if (Layer.isShowNewLayerTableView()) {
        layer.showTableView();
      }
    }
  }

  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "Map Guide Web Server Record Layer", (config) -> {
      return new MapGuideWebServerRecordLayer(config);
    });

    MenuFactory.addMenuInitializer(() -> {
      MenuFactory.addMenuInitializer(MapGuideFeatureLayer.class, (menu) -> {
        menu.addMenuItem("default", "Add Layer", "map:add", MapGuideWebServer::actionAddLayer,
          false);
      });
    });
  }
}
