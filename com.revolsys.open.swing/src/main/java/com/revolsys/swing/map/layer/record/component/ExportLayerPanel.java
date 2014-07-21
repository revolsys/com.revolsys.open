package com.revolsys.swing.map.layer.record.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.gpx.GpxWriter;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class ExportLayerPanel extends BasePanel {

  private final AbstractRecordLayer layer;

  private TogglePanel recordsFilterType;

  public ExportLayerPanel(final AbstractRecordLayer layer) {
    super(new BorderLayout());
    this.layer = layer;

    createRecordsFilterPanel();

    final BasePanel centre = new BasePanel();
    centre.add(this.recordsFilterType);
  }

  private void createRecordsFilterPanel() {
    final List<Action> actions = new ArrayList<>();

    actions.add(new I18nAction("allRecords", null, "All Records",
      SilkIconLoader.getIcon("table_filter")));

    actions.add(new I18nAction("mapRecords", null, "Records on Map",
      SilkIconLoader.getIcon("map_filter")));

    actions.add(new I18nAction("selectedRecords", null, "Selected Records",
      SilkIconLoader.getIcon("filter_selected")));

    this.recordsFilterType = new TogglePanel("recordsFilterType", "",
      new Dimension(28, 28), actions);
  }

  public void doExport() {
    final List<LayerRecord> records = this.layer.getSelectedRecords();
    if (!records.isEmpty()) {
      try {
        try (
            GpxWriter writer = new GpxWriter(new File("/Users/paustin/Desktop/"
                + getName() + ".gpx"))) {
          for (final LayerRecord record : records) {
            writer.write(record);
          }
        }
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
