package com.revolsys.swing.map.layer.arcgisrest;

import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.record.io.format.esri.rest.map.FeatureLayer;
import com.revolsys.record.io.format.esri.rest.map.TileInfo;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.WebServiceConnectionTrees;
import com.revolsys.util.Property;

public interface ArcGisRestServer {

  String J_TYPE_RECORD = "arcGisRestServerRecordLayer";

  static void actionAddRecordLayer(final FeatureLayer layerDescription) {
    final LayerGroup layerGroup = WebServiceConnectionTrees.getLayerGroup(layerDescription);
    if (layerGroup != null) {
      final ArcGisRestServerRecordLayer layer = new ArcGisRestServerRecordLayer(layerDescription);
      layerGroup.addLayer(layer);
      if (AbstractLayer.isShowNewLayerTableView()) {
        layer.showTableView();
      }
    }
  }

  static void actionAddTileCacheLayer(final BaseMapLayerGroup parent) {
    final ValueField dialog = new ValueField();
    dialog.setTitle("Add ArcGIS Tile Cache");

    SwingUtil.addLabel(dialog, "URL");
    final TextField urlField = new TextField("url", 50);
    dialog.add(urlField);

    GroupLayouts.makeColumns(dialog, 2, true, true);

    dialog.setSaveAction(() -> {
      final String url = urlField.getText();
      if (Property.hasValue(url)) {
        final ArcGisRestServerTileCacheLayer layer = new ArcGisRestServerTileCacheLayer();
        layer.setUrl(url);
        layer.setVisible(true);
        parent.addLayer(layer);
      }
    });

    dialog.showDialog();
  }

  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("arcGisRestServerRecordLayer",
      "Arc GIS REST Server Record Layer", (config) -> {
        return new ArcGisRestServerRecordLayer(config);
      });

    MapObjectFactoryRegistry.newFactory("arcGisRestServerTileLayer",
      "Arc GIS REST Server Tile Cache Layer", (config) -> {
        return new ArcGisRestServerTileCacheLayer(config);
      });

    MapObjectFactoryRegistry.newFactory("arcgisServerRest", "Arc GIS REST Server Tile Cache Layer",
      (config) -> {
        return new ArcGisRestServerTileCacheLayer(config);
      });

    ArcGisRestServer.initMenus();
  }

  public static void initMenus() {
    MenuFactory.addMenuInitializer(FeatureLayer.class, (menu) -> {
      menu.addMenuItem("default", "Add Layer", "map:add", ArcGisRestServer::actionAddRecordLayer,
        false);
    });

    MenuFactory.addMenuInitializer(BaseMapLayerGroup.class, (menu) -> {
      menu.addMenuItem("group", "Add ArcGIS Tile Cache", Icons.getIconWithBadge("map", "add"),
        ArcGisRestServer::actionAddTileCacheLayer, false);
    });

    MenuFactory.addMenuInitializer(TileInfo.class, (menu) -> {
      BaseMapLayer.addNewLayerMenu(menu, (final TileInfo tileInfo) -> {
        return new ArcGisRestServerTileCacheLayer(tileInfo);
      });
    });
  }

}
