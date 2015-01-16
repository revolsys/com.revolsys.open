package com.revolsys.swing.map.layer.record.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.RecordIo;
import com.revolsys.data.io.RecordReaderFactory;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.functions.EnvelopeIntersects;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.map.action.AddFileLayerAction;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class ExportLayerRecordsPanel extends BasePanel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  private TogglePanel recordsFilterType;

  private Resource exportResource;

  public ExportLayerRecordsPanel(final AbstractRecordLayer layer) {
    super(new BorderLayout());
    this.layer = layer;

    createRecordsFilterPanel();
    addFileChooser(this);
    final BasePanel centre = new BasePanel();
    centre.add(this.recordsFilterType);
  }

  private void addFileChooser(final JComponent component) {

    final JFileChooser fileChooser = SwingUtil.createFileChooser(getClass(),
        "currentDirectory");

    final List<FileFilter> recordFileFilters = new ArrayList<FileFilter>();
    final Set<String> allRecordExtensions = new TreeSet<String>();
    AddFileLayerAction.getFileFilters(recordFileFilters, allRecordExtensions,
      RecordReaderFactory.class);

    final FileNameExtensionFilter allRecordFilter = AddFileLayerAction.createFileFilter(
      "All Vector/Data files", allRecordExtensions);
    fileChooser.addChoosableFileFilter(allRecordFilter);

    for (final FileFilter fileFilter : recordFileFilters) {
      fileChooser.addChoosableFileFilter(fileFilter);
    }

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(allRecordFilter);
    component.add(fileChooser);
  }

  private void createRecordsFilterPanel() {
    final List<Action> actions = new ArrayList<>();

    actions.add(new I18nAction("allRecords", null, "All Records",
      Icons.getIcon("table_filter")));

    actions.add(new I18nAction("mapRecords", null, "Records on Map",
      Icons.getIcon("map_filter")));

    actions.add(new I18nAction("selectedRecords", null, "Selected Records",
      Icons.getIcon("filter_selected")));

    this.recordsFilterType = new TogglePanel("recordsFilterType", "",
      new Dimension(28, 28), actions);
  }

  public void doExport() {
    this.exportResource = new FileSystemResource("/Users/paustin/Desktop/"
        + getName() + ".gpx");

    final RecordDefinition recordDefinition = getRecordDefinition();
    final Query query = new Query(recordDefinition);
    final List<String> idFieldNames = recordDefinition.getIdFieldNames();
    query.setOrderByColumns(idFieldNames);

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
        Writer<Record> writer = RecordIo.recordWriter(
          this.layer.getRecordDefinition(), this.exportResource)) {
      for (final LayerRecord record : records) {
        writer.write(record);
      }
    }
  }
}
