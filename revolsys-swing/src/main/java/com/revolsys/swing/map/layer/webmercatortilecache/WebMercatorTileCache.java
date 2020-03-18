package com.revolsys.swing.map.layer.webmercatortilecache;

import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Property;

public interface WebMercatorTileCache {

  static void actionAddLayer(final BaseMapLayerGroup parent) {
    final ValueField dialog = new ValueField();
    dialog.setTitle("Add Web Mercator Tile Cache Layer");

    SwingUtil.addLabel(dialog, "URL");
    final TextField urlField = new TextField("url", 50);
    dialog.add(urlField);

    GroupLayouts.makeColumns(dialog, 2, true, true);

    dialog.setSaveAction(() -> {
      final String url = urlField.getText();
      if (Property.hasValue(url)) {
        final WebMercatorTileCacheLayer layer = new WebMercatorTileCacheLayer();
        layer.setUrl(url);
        layer.setVisible(true);
        parent.addLayer(layer);
      }
    });
    dialog.showDialog();
  }

  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("webMercatorTileCacheLayer", "Web Mercator Tile Cache",
      (config) -> {
        return new WebMercatorTileCacheLayer(config);
      });

    MenuFactory.addMenuInitializer(BaseMapLayerGroup.class, (menu) -> {
      menu.addMenuItem("group", "Add Web Mercator Tile Cache Layer",
        Icons.getIconWithBadge("map", "add"), WebMercatorTileCache::actionAddLayer, false);
    });
  }

}
