package com.revolsys.swing.map.layer.ogc.wms;

import java.util.function.Function;

import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.menu.MenuFactory;

public class OgcWms {
  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("ogcWmsImageLayer", "OGC WMS Image Layer", (config) -> {
      return new OgcWmsImageLayer(config);
    });

    MenuFactory.addMenuInitializer(WmsLayerDefinition.class, (menu) -> {
      final Function<WmsLayerDefinition, BaseMapLayer> baseMapLayerFactory = OgcWmsImageLayer::new;
      BaseMapLayer.addNewLayerMenu(menu, baseMapLayerFactory);
    });

  }
}
