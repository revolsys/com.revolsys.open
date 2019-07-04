package com.revolsys.swing.map.layer.record;

import java.util.List;

import javax.swing.JMenu;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.overlay.ShortestRouteOverlay;
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
      final ShortestRouteOverlay shortestRouteOverlay = map
        .getMapOverlay(ShortestRouteOverlay.class);
      if (shortestRouteOverlay.getLayer() == layer) {
        final List<LayerRecord> records = shortestRouteOverlay.getAllRecords();
        if (!records.isEmpty()) {
          clear();
          addMenuItem("zoom", "Zoom to Route", "magnifier", () -> {
            final BoundingBox boundingBox = BoundingBox.bboxNew(records);
            map.zoomToBoundingBox(boundingBox);
          });
          addMenuItem("select", "Set as Select Records", "cursor", () -> {
            layer.setSelectedRecords(records);
            layer.showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED, true);
          });
          addMenuItem("select", "Add to Selected Records", "cursor:add",
            () -> layer.addSelectedRecords(records));
          return super.newComponent();
        }
      }
    }
    return null;
  }
}
