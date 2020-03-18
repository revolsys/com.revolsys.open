package com.revolsys.swing.map.layer.record;

import java.util.List;

import javax.swing.JMenu;

import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.overlay.ShortestPathOverlay;
import com.revolsys.swing.menu.MenuFactory;

public class RecordShortestPathMenu extends MenuFactory {

  public RecordShortestPathMenu() {
    setName("Shortest Path");
  }

  @Override
  public MenuFactory clone() {
    return new RecordShortestPathMenu();
  }

  @Override
  public JMenu newComponent() {
    final Object menuSource = MenuFactory.getMenuSource();
    if (menuSource instanceof AbstractRecordLayer) {
      final AbstractRecordLayer layer = (AbstractRecordLayer)menuSource;
      final MapPanel map = layer.getMapPanel();
      final ShortestPathOverlay routingOverlay = map.getMapOverlay(ShortestPathOverlay.class);
      if (routingOverlay.getLayer() == layer) {
        final List<LayerRecord> records = routingOverlay.getAllRecords();
        if (!records.isEmpty()) {
          clear();
          addMenuItem("zoom", "Zoom to Shortest Path", "magnifier",
            routingOverlay::actionZoomToRecords);
          addMenuItem("select", "Set as Select Records", "cursor",
            routingOverlay::actionSelectRecords);
          addMenuItem("select", "Add to Selected Records", "cursor:add",
            routingOverlay::actionAddToSelectRecords);
          return super.newComponent();
        }
      }
    }
    return null;
  }
}
