package com.revolsys.swing.map.layer.record.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.model.Direction;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.LineString;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.action.enablecheck.SimpleEnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTableCellEditor;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.lambda.column.ColumnBasedTableModel;
import com.revolsys.swing.table.lambda.column.LayerRecordTableModelColumn;
import com.revolsys.swing.table.record.model.RecordListTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.undo.CreateRecordUndo;
import com.revolsys.swing.undo.DeleteLayerRecordUndo;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class MergeRecordsDialog extends JDialog {

  private static class MergeableRecord extends ArrayRecord {
    final List<LayerRecord> originalRecords = new ArrayList<>();

    final List<Boolean> mergedForwards = new ArrayList<>();

    private MergeableRecord(final LayerRecord originalRecord) {
      super(originalRecord);
      setGeometryValue(originalRecord);
      this.originalRecords.add(originalRecord);
      this.mergedForwards.add(true);
    }

    public MergeableRecord(final Record mergedRecord, final MergeableRecord record1,
      final boolean forwards1, final MergeableRecord record2, final boolean forwards2) {
      super(mergedRecord);
      setGeometryValue(mergedRecord);
      setOriginalRecords(record1, forwards1);
      setOriginalRecords(record2, forwards2);
    }

    private List<MergeRecordFieldMessage> getMessages(final MergeRecordsDialog dialog) {
      final List<Record> orientedRecords = new ArrayList<>();
      for (int i = 0; i < this.originalRecords.size(); i++) {
        orientedRecords.add(getOrientedRecord(i));
      }
      final List<MergeRecordFieldMessage> messages = new ArrayList<>();
      final List<String> fieldNames = getFieldNames();
      for (final String fieldName : fieldNames) {
        final Object mergedValue = getValue(fieldName);
        final MergeRecordFieldMessage message = new MergeRecordFieldMessage();
        messages.add(message);
        if (isIdField(fieldName)) {
          // Ignore difference in ID fields
        } else if (isGeometryField(fieldName)) {
          // Ignore difference in Geometry fields
        } else if (dialog.ignoreDifferentFieldNames.contains(fieldName)) {
          // Ignore difference
        } else {
          final boolean allowChange = false; // TODO
          for (final Record originalRecord : orientedRecords) {
            final Object originalValue = originalRecord.getValue(fieldName);
            if (!DataType.equal(mergedValue, originalValue)) {
              if (allowChange) {
                message.setMessage(TYPE_WARNING, "Allowed Different");
              } else {
                message.setMessage(TYPE_ERROR, "Error Different");
              }
              break;
            }
          }
        }
      }
      return messages;
    }

    private Record getOrientedRecord(final int index) {
      final LayerRecord record = this.originalRecords.get(index);
      if (this.mergedForwards.get(index)) {
        return record;
      } else {
        final Record reverseRecord = new ArrayRecord(record);
        DirectionalFields.reverseRecord(reverseRecord);
        return reverseRecord;
      }
    }

    private void setOriginalRecords(final MergeableRecord record, final boolean forwards) {
      this.originalRecords.addAll(record.originalRecords);
      if (forwards) {
        this.mergedForwards.addAll(record.mergedForwards);
      } else {
        for (final Boolean oldForwards : record.mergedForwards) {
          this.mergedForwards.add(!oldForwards);
        }
      }
    }
  }

  private static class MergeRecordFieldMessage implements Comparable<MergeRecordFieldMessage> {

    int typeId;

    String message;

    @Override
    public int compareTo(final MergeRecordFieldMessage message) {
      final int compare = -Integer.compare(this.typeId, message.typeId);
      if (compare == 0) {
        if (this.message == null) {
          if (message.message == null) {
            return 0;
          } else {
            return 1;
          }
        } else {
          if (message.message == null) {
            return -1;
          } else {
            return this.message.compareTo(message.message);
          }
        }
      }
      return compare;
    }

    public void setMessage(final int typeId, final String message) {
      this.typeId = typeId;
      this.message = message;
    }

    @Override
    public String toString() {
      return this.message;
    }

  }

  private static class RecordMerger {

    private final AbstractRecordLayer layer;

    String errorMessage = "";

    final List<MergeableRecord> records = new ArrayList<>();

    private RecordMerger(final AbstractRecordLayer layer) {
      this.layer = layer;
    }

    public void run(final ProgressMonitor progress) {
      try {
        final List<LayerRecord> originalRecords = this.layer.getMergeableSelectedRecords();
        this.records.clear();
        final DataType geometryType = this.layer.getGeometryType();
        if (originalRecords.size() < 2) {
          this.errorMessage = " at least two records must be selected to merge.";
        } else if (!GeometryDataTypes.LINE_STRING.equals(geometryType)
          && !GeometryDataTypes.MULTI_LINE_STRING.equals(geometryType)) {
          this.errorMessage = "Merging " + geometryType + " not currently supported";
        } else {
          final RecordGraph graph = new RecordGraph();
          for (final LayerRecord originalRecord : originalRecords) {
            Geometry geometry = originalRecord.getGeometry();
            if (geometry != null && !geometry.isEmpty()) {
              geometry = this.layer.getGeometryFactory().geometry(LineString.class, geometry);
              if (geometry instanceof LineString) {
                final MergeableRecord mergeableRecord = new MergeableRecord(originalRecord);
                this.records.add(mergeableRecord);
                graph.addEdge(mergeableRecord);
              }
            }
          }
          for (final Node<Record> node : graph.nodes()) {
            if (node != null) {
              final List<Edge<Record>> edges = node.getEdges();
              if (edges.size() == 2) {
                final Edge<Record> edge1 = edges.get(0);
                final MergeableRecord record1 = (MergeableRecord)edge1.getObject();
                final Edge<Record> edge2 = edges.get(1);
                final MergeableRecord record2 = (MergeableRecord)edge2.getObject();
                if (record1 != record2 && !edge1.isLoop() && !edge2.isLoop()) {
                  final LineString line1 = record1.getGeometry();
                  final Record mergedRecord = this.layer.getMergedRecord(node, record1, record2);
                  final LineString line = mergedRecord.getGeometry();

                  final Direction direction1 = edge1.getDirection(node);
                  final Direction direction2 = edge2.getDirection(node);

                  final MergeableRecord record;
                  if (direction1.isBackwards()) {
                    if (direction2.isBackwards()) {
                      // --> * <-- TODO
                      if (line.equalsVertex(2, 0, line1, 0)) {
                        // Reverse line2
                        record = new MergeableRecord(mergedRecord, record1, true, record2, false);
                      } else {
                        // Reverse line1
                        record = new MergeableRecord(mergedRecord, record2, true, record1, false);
                      }
                    } else {
                      // --> * -->
                      record = new MergeableRecord(mergedRecord, record1, true, record2, true);
                    }
                  } else {
                    if (direction2.isBackwards()) {
                      // <-- * <--
                      record = new MergeableRecord(mergedRecord, record2, true, record1, true);
                    } else {
                      // <-- * --> TODO
                      if (line.equalsVertex(2, 0, line1, line1.getLastVertexIndex())) {
                        // Reverse line1
                        record = new MergeableRecord(mergedRecord, record1, false, record2, true);
                      } else {
                        // Reverse line2
                        record = new MergeableRecord(mergedRecord, record2, false, record1, true);
                      }
                    }
                  }
                  this.records.add(record);
                  this.records.remove(record1);
                  this.records.remove(record2);

                  graph.addEdge(record);
                  edge1.remove();
                  edge2.remove();

                }
              }
            }
          }
        }
      } catch (final Throwable e) {
        Logs.error(this, "Error " + this, e);
      }
    }
  }

  private static final int TYPE_ERROR = 2;

  private static final int TYPE_WARNING = 1;

  private static final long serialVersionUID = 1L;

  public static void showDialog(final AbstractRecordLayer layer) {
    final RecordMerger recordMerger = new RecordMerger(layer);
    final Window window = SwingUtil.getWindowAncestor(layer.getMapPanel());
    ProgressMonitor.background(window, "Merge " + layer.getName() + " Records", null,
      recordMerger::run, 100, () -> {
        new MergeRecordsDialog(window, layer, recordMerger) //
          .setVisible(true);
      });
  }

  private final AbstractRecordLayer layer;

  private TabbedPane mergedRecordsPanel;

  private final UndoManager undoManager;

  private final RecordMerger recordMerger;

  private final Collection<String> ignoreDifferentFieldNames;

  private MergeRecordsDialog(final Window window, final AbstractRecordLayer layer,
    final RecordMerger recordMerger) {
    super(window, "Merge " + layer.getName() + " Records", ModalityType.APPLICATION_MODAL);
    this.undoManager = layer.getMapPanel().getUndoManager();
    this.layer = layer;
    this.recordMerger = recordMerger;
    this.ignoreDifferentFieldNames = layer.getProperty("mergeRecordsIgnoreDifferentFieldNames",
      Collections.emptySet());
    initDialog();
  }

  private void addMessageHighlighter(final BaseJTable table,
    final List<MergeRecordFieldMessage> messages, final int typeId, final Color color1,
    final Color color2) {
    table.addColorHighlighter((rowIndex, columnIndex) -> {
      if (columnIndex > 1 && columnIndex < 4) {
        final MergeRecordFieldMessage message = messages.get(rowIndex);
        if (columnIndex == 3 || message.message != null) {
          return typeId == message.typeId;
        }
      }
      return false;
    }, color1, color2);
  }

  private void addTab(final String title, final JPanel panel) {
    this.mergedRecordsPanel.addClosableTab(title, null, panel, () -> {
      if (this.mergedRecordsPanel.getTabCount() == 0) {
        setVisible(false);
      }
    });
  }

  protected void initDialog() {
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setMinimumSize(new Dimension(600, 100));

    final BasePanel panel = new BasePanel(new BorderLayout());
    add(new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    this.mergedRecordsPanel = new TabbedPane();
    panel.add(this.mergedRecordsPanel, BorderLayout.CENTER);

    pack();
    SwingUtil.autoAdjustPosition(this);
    setMergedRecords(this.recordMerger.errorMessage);
  }

  private TablePanel newTable(final ColumnBasedTableModel model,
    final MergeableRecord mergeableRecord, final List<LayerRecord> originalRecords,
    final List<MergeRecordFieldMessage> messages) {
    final TablePanel tablePanel = model.newTablePanel();
    final ToolBar toolBar = tablePanel.getToolBar();

    final SimpleEnableCheck canMergeEnableCheck = new SimpleEnableCheck(true);
    updateCanMerge(canMergeEnableCheck, messages);
    toolBar.addButton("default", "Merge Records", canMergeEnableCheck, () -> {
      final MultipleUndo multipleUndo = new MultipleUndo();
      final CreateRecordUndo createRecordUndo = new CreateRecordUndo(this.layer, mergeableRecord);
      multipleUndo.addEdit(createRecordUndo);
      for (final LayerRecord originalRecord : originalRecords) {
        final DeleteLayerRecordUndo deleteRecordUndo = new DeleteLayerRecordUndo(originalRecord);
        multipleUndo.addEdit(deleteRecordUndo);
      }
      if (this.undoManager == null) {
        multipleUndo.redo();
      } else {
        this.undoManager.addEdit(multipleUndo);
      }
      setVisible(false);
    });
    final BaseJTable table = tablePanel.getTable();

    table.addColorHighlighter((rowIndex, columnIndex) -> {
      if (columnIndex > 3) {
        final Record originalRecord = mergeableRecord.getOrientedRecord(columnIndex - 4);
        if (!originalRecord.isIdField(rowIndex) && !originalRecord.isGeometryField(rowIndex)) {
          final Object originalValue = originalRecord.getValue(rowIndex);

          final Object mergedValue = mergeableRecord.getValue(rowIndex);
          return !DataType.equal(originalValue, mergedValue);
        }
      }
      return false;
    }, WebColors.Moccasin, WebColors.DarkOrange);
    addMessageHighlighter(table, messages, 0, WebColors.LightGreen, WebColors.Green);
    addMessageHighlighter(table, messages, TYPE_WARNING, WebColors.Moccasin, WebColors.DarkOrange);
    addMessageHighlighter(table, messages, TYPE_ERROR, WebColors.Pink, WebColors.Red);

    table.setSortOrder(2, SortOrder.ASCENDING);

    table//
      .setColumnWidth(0, 40) //
      .setColumnPreferredWidth(1, 150) //
      .setColumnPreferredWidth(2, 110) //
    ;
    for (int i = 0; i <= originalRecords.size(); i++) {
      table.setColumnPreferredWidth(3 + i, 120);
    }
    return tablePanel;
  }

  private ColumnBasedTableModel newTableModel(final MergeableRecord mergeableRecord,
    final List<MergeRecordFieldMessage> messages) {
    final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
    final int fieldCount = recordDefinition.getFieldCount();

    final List<String> fieldTitles = recordDefinition.getFieldTitles();

    final ColumnBasedTableModel model = new ColumnBasedTableModel()//
      .setRowCount(fieldCount) //
      .addColumnRowIndex() //
      .addColumnValues("Name", String.class, fieldTitles) //
      .addColumnValues("Message", MergeRecordFieldMessage.class, messages) //
    ;
    model.addColumn( //
      new LayerRecordTableModelColumn("Merged Record", this.layer, mergeableRecord, true)//
        .setCellEditor(table -> new RecordLayerTableCellEditor(table, this.layer) {
          @Override
          protected String getColumnFieldName(final int rowIndex, final int columnIndex) {
            return recordDefinition.getFieldName(rowIndex);
          }
        }) //
    );

    for (int i = 0; i < mergeableRecord.originalRecords.size(); i++) {
      final Record record = mergeableRecord.getOrientedRecord(i);
      model.addColumnRecord("Record " + (i + 1), record, false);
    }
    return model;
  }

  private void setMergedRecord(final MergeableRecord mergedRecord) {
    final List<LayerRecord> originalRecords = mergedRecord.originalRecords;
    final List<MergeRecordFieldMessage> messages = mergedRecord.getMessages(this);
    final ColumnBasedTableModel model = newTableModel(mergedRecord, messages);
    final TablePanel tablePanel = newTable(model, mergedRecord, originalRecords, messages);
    addTab(originalRecords.size() + " Mergable", tablePanel);
  }

  private void setMergedRecords(String errorMessage) {
    final List<LayerRecord> nonMergeableRecords = new ArrayList<>();

    for (final MergeableRecord mergeableRecord : this.recordMerger.records) {
      if (mergeableRecord.originalRecords.size() == 1) {
        nonMergeableRecords.addAll(mergeableRecord.originalRecords);
      } else {
        setMergedRecord(mergeableRecord);
      }
    }
    if (!nonMergeableRecords.isEmpty() || Property.hasValue(errorMessage)) {
      final TablePanel tablePanel = RecordListTableModel.newPanel(this.layer, nonMergeableRecords);
      final RecordListTableModel tableModel = tablePanel.getTableModel();
      tableModel.setEditable(false);
      tablePanel.setPreferredSize(new Dimension(100, 50 + nonMergeableRecords.size() * 22));

      final JPanel panel = new JPanel(new BorderLayout());
      if (!Property.hasValue(errorMessage)) {
        errorMessage = "The following records could not be merged and will not be modified.";
      }
      final JLabel unMergeLabel = new JLabel(
        "<html><p style=\"color:red\">" + errorMessage + "</p></html>");
      panel.add(unMergeLabel, BorderLayout.NORTH);
      panel.add(tablePanel, BorderLayout.CENTER);

      addTab(nonMergeableRecords.size() + " Non-Mergable", panel);
    }
    SwingUtil.autoAdjustPosition(this);
  }

  @Override
  public String toString() {
    return getTitle();
  }

  private void updateCanMerge(final SimpleEnableCheck enableCheck,
    final List<MergeRecordFieldMessage> messages) {
    for (final MergeRecordFieldMessage message : messages) {
      if (message.typeId == TYPE_ERROR) {
        enableCheck.setEnabled(false);
        return;
      }
    }
    enableCheck.setEnabled(true);
  }

}
