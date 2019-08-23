package com.revolsys.swing.map.layer.record;

import java.util.List;

import javax.swing.JMenu;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.overlay.RoutingOverlay;
import com.revolsys.swing.menu.MenuFactory;

public class RecordRouteMenu extends MenuFactory {

  public RecordRouteMenu() {
    setName("Route");
  }

  @Override
  public MenuFactory clone() {
    return new RecordRouteMenu();
  }

  @Override
  public JMenu newComponent() {
    final Object menuSource = MenuFactory.getMenuSource();
    if (menuSource instanceof AbstractRecordLayer) {
      final AbstractRecordLayer layer = (AbstractRecordLayer)menuSource;
      final MapPanel map = layer.getMapPanel();
      final RoutingOverlay routingOverlay = map.getMapOverlay(RoutingOverlay.class);
      if (routingOverlay.getLayer() == layer) {
        final List<LayerRecord> records = routingOverlay.getAllRecords();
        if (!records.isEmpty()) {
          clear();
          addMenuItem("zoom", "Zoom to Route", "magnifier", () -> {
            final BoundingBox boundingBox = BoundingBox.bboxNew(records);
            map.zoomToBoundingBox(boundingBox);
          });
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
