package com.revolsys.swing.map.layer.bing;

import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.menu.MenuFactory;

public interface Bing {
  static void actionAddLayer(final BaseMapLayerGroup parent) {
    final ValueField dialog = new ValueField();
    dialog.setTitle("Add Bing Layer");
    dialog.setIconImage("bing");

    SwingUtil.addLabel(dialog, "Imagery Set");
    final ComboBox<ImagerySet> imagerySetField = ComboBox.newComboBox("imagerySet",
      ImagerySet.values());
    dialog.add(imagerySetField);

    GroupLayouts.makeColumns(dialog, 2, true, true);

    dialog.setSaveAction(() -> {
      final BingLayer layer = new BingLayer();
      final ImagerySet imagerySet = imagerySetField.getSelectedItem();
      layer.setImagerySet(imagerySet);
      layer.setVisible(true);
      parent.addLayer(layer);
    });

    dialog.showDialog();
  }

  static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("bing", "Bing Tile Cache", (config) -> {
      return new BingLayer(config);
    });

    MenuFactory.addMenuInitializer(BaseMapLayerGroup.class, (menu) -> {
      menu.addMenuItem("group", "Add Bing Layer", "bing", Bing::actionAddLayer, false);
    });

  }
}
