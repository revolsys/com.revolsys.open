package com.revolsys.swing.map.layer;

import java.util.function.Function;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.WebServiceConnectionTrees;
import com.revolsys.webservice.WebServiceResource;

public interface BaseMapLayer extends Layer {

  static <S extends WebServiceResource> void addNewLayerMenu(final MenuFactory tileInfoMenu,
    final Function<S, BaseMapLayer> factory) {
    tileInfoMenu.addMenuItem("layer", "Add Base Map Layer", "map:add", (final S treeItem) -> {
      final Project project = Project.get();
      if (project != null) {
        final BaseMapLayerGroup baseMaps = project.getBaseMapLayers();
        if (baseMaps != null) {
          final BaseMapLayer layer = factory.apply(treeItem);
          layer.setVisible(true);
          baseMaps.addLayer(layer);
        }
      }
    }, false);
    tileInfoMenu.addMenuItem("layer", "Add Layer", "map:add", (final S treeItem) -> {
      final LayerGroup layerGroup = WebServiceConnectionTrees.getLayerGroup(treeItem);
      if (layerGroup != null) {
        final BaseMapLayer layer = factory.apply(treeItem);
        layer.setVisible(true);
        layerGroup.addLayer(layer);
      }
    }, false);
  }

}
