package com.revolsys.swing.map.layer.record.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.RecordIoFactories;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.functions.EnvelopeIntersects;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class ExportLayerRecordsPanel extends BasePanel {

  private final AbstractRecordLayer layer;

  private TogglePanel recordsFilterType;

  private Resource exportResource;

  public ExportLayerRecordsPanel(final AbstractRecordLayer layer) {
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
    this.exportResource = new FileSystemResource("/Users/paustin/Desktop/"
        + getName() + ".gpx");

    final RecordDefinition recordDefinition = getRecordDefinition();
    final Query query = new Query(recordDefinition);
    final List<String> idAttributeNames = recordDefinition.getIdAttributeNames();
    query.setOrderByColumns(idAttributeNames);

    final Object filterType = this.recordsFilterType.getFieldValue();
    if ("allRecords".equals(filterType)) {
      final List<LayerRecord> records = this.layer.query(query);
      writeRecords(records);
    } else if ("mapRecords".equals(filterType)) {
      final Project project = this.layer.getProject();
      final BoundingBox boundingBox = project.getViewBoundingBox();
      final EnvelopeIntersects filter = F.envelopeIntersects(recordDefinition,
        boundingBox);
      query.setWhereCondition(filter);
      final List<LayerRecord> records = this.layer.query(query);
      writeRecords(records);
    } else if ("selectedRecords".equals(filterType)) {
      final List<LayerRecord> records = this.layer.getSelectedRecords();
      writeRecords(records);
    }
  }

  public RecordDefinition getRecordDefinition() {
    return this.layer.getRecordDefinition();
  }

  private void writeRecords(final Iterable<LayerRecord> records) {
    try (
        Writer<Record> writer = RecordIoFactories.recordWriter(
          this.layer.getRecordDefinition(), this.exportResource)) {
      for (final LayerRecord record : records) {
        writer.write(record);
      }
    }
  }
}
