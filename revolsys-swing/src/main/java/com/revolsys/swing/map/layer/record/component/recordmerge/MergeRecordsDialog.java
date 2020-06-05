package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTableCellEditor;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.lambda.column.ColumnBasedTableModel;
import com.revolsys.swing.table.lambda.column.LayerRecordTableModelColumn;
import com.revolsys.swing.table.lambda.column.RecordTableModelColumn;
import com.revolsys.swing.table.record.model.RecordListTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.CreateRecordUndo;
import com.revolsys.swing.undo.DeleteLayerRecordUndo;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class MergeRecordsDialog extends BaseDialog {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final AbstractRecordLayer layer) {
    final RecordMerger recordMerger = new RecordMerger(layer);
    final Window window = SwingUtil.getWindowAncestor(layer.getMapPanel());
    ProgressMonitor.background("Merge " + layer.getName() + " Records", null, recordMerger::run,
      100, () -> {
        new MergeRecordsDialog(window, layer, recordMerger) //
          .setVisible(true);
      });
  }

  private final AbstractRecordLayer layer;

  private TabbedPane mergedRecordsPanel;

  private final UndoManager undoManager;

  private final RecordMerger recordMerger;

  private MergeRecordsDialog(final Window window, final AbstractRecordLayer layer,
    final RecordMerger recordMerger) {
    super(window, "Merge " + layer.getName() + " Records", ModalityType.DOCUMENT_MODAL);
    this.undoManager = layer.getMapPanel().getUndoManager();
    this.layer = layer;
    this.recordMerger = recordMerger;
    recordMerger.dialog = this;
    initDialog();
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

  private TablePanel newTable(final MergeableRecord mergeableRecord) {
    final ColumnBasedTableModel model = newTableModel(mergeableRecord);

    final TablePanel tablePanel = model.newTablePanel();

    final String tabTitle = mergeableRecord.originalRecords.size() + " Mergeable";
    this.mergedRecordsPanel.addClosableTab(tabTitle, null, tablePanel, () -> {
      if (this.mergedRecordsPanel.getTabCount() == 0) {
        setVisible(false);
      }
    });

    final ToolBar toolBar = tablePanel.getToolBar();

    toolBar.addButton("default", "Merge Records", "table_row_merge",
      mergeableRecord.canMergeEnableCheck, () -> {
        final MultipleUndo multipleUndo = new MultipleUndo();
        multipleUndo.addEdit(new AbstractUndoableEdit() {
          @Override
          public boolean canRedo() {
            return true;
          }

          @Override
          public boolean canUndo() {
            return true;
          }

          @Override
          protected void undoDo() {
            MergeRecordsDialog.this.layer.fireRecordsChanged();
          }
        });
        final CreateRecordUndo createRecordUndo = new CreateRecordUndo(this.layer, mergeableRecord,
          true);
        multipleUndo.addEdit(createRecordUndo);
        for (final MergeOriginalRecord originalRecord : mergeableRecord.originalRecords) {
          final DeleteLayerRecordUndo deleteRecordUndo = new DeleteLayerRecordUndo(
            originalRecord.originalRecord);
          multipleUndo.addEdit(deleteRecordUndo);
        }
        multipleUndo.addEdit(new AbstractUndoableEdit() {
          @Override
          public boolean canRedo() {
            return true;
          }

          @Override
          public boolean canUndo() {
            return true;
          }

          @Override
          protected void redoDo() {
            MergeRecordsDialog.this.layer.fireRecordsChanged();
          }
        });
        if (this.undoManager == null) {
          multipleUndo.redo();
        } else {
          this.undoManager.addEdit(multipleUndo);
        }
        this.mergedRecordsPanel.remove(tablePanel);
        if (this.mergedRecordsPanel.getTabCount() == 0) {
          setVisible(false);
        }
      });
    final BaseJTable table = tablePanel.getTable();

    newTableHighlighter(table, mergeableRecord);

    table.setSortOrder(2, SortOrder.ASCENDING);

    table//
      .setColumnWidth(0, 40) //
      .setColumnPreferredWidth(1, 150) //
      .setColumnPreferredWidth(2, 110) //
    ;
    for (int i = 0; i <= mergeableRecord.originalRecords.size(); i++) {
      table.setColumnPreferredWidth(3 + i, 120);
    }
    return tablePanel;
  }

  private void newTableHighlighter(final BaseJTable table, final MergeableRecord mergeableRecord) {
    table.addHighlighter((renderer, adaptor, fieldIndex, columnIndex) -> {
      if (columnIndex >= 3) {
        final boolean selected = adaptor.isSelected();
        final boolean even = adaptor.row % 2 == 0;
        if (columnIndex == 3) {
          final MergeFieldMatchType rowMatchType = mergeableRecord.getMatchType(fieldIndex);
          rowMatchType.setColor(renderer, selected, even);
        } else if (columnIndex > 3) {
          final int recordIndex = columnIndex - 4;
          final MergeOriginalRecord originalRecord = mergeableRecord.getOriginalRecord(recordIndex);
          final MergeFieldOriginalFieldState fieldState = originalRecord.getFieldState(fieldIndex);
          final MergeFieldMatchType matchType = fieldState.getMatchType();
          if (matchType != MergeFieldMatchType.EQUAL) {
            matchType.setColor(renderer, selected, even);
            final String message = fieldState.getMessage();
            if (message != null) {
              ((JComponent)renderer)
                .setToolTipText("<html><b color='red'>" + message + "</b></html>");
            }
          }
        }
      }
      return renderer;
    });
  }

  private ColumnBasedTableModel newTableModel(final MergeableRecord mergeableRecord) {
    mergeableRecord.mergeValidateFields(true);
    final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
    final int fieldCount = recordDefinition.getFieldCount();

    final List<String> fieldTitles = recordDefinition.getFieldTitles();

    final List<MergeFieldMatchType> matchTypes = mergeableRecord.getMatchTypes();

    final ColumnBasedTableModel model = new ColumnBasedTableModel()//
      .setRowCount(fieldCount) //
      .addColumnRowIndex() //
      .addColumnValues("Name", String.class, fieldTitles) //
      .addColumnValues("Match Type", MergeFieldMatchType.class, matchTypes) //
    ;
    model.addColumn( //
      new LayerRecordTableModelColumn("Merged Record", this.layer, mergeableRecord, true) {
        @Override
        public BaseJPopupMenu getMenu(final int fieldIndex) {
          final MergeFieldMatchType matchType = matchTypes.get(fieldIndex);
          if (matchType == MergeFieldMatchType.NOT_EQUAL
            || matchType == MergeFieldMatchType.END_FIELD_NOT_VALID
            || matchType == MergeFieldMatchType.OVERRIDDEN) {
            final BaseJPopupMenu menu = new BaseJPopupMenu();
            final Set<Object> values = new HashSet<>();
            if (mergeableRecord.isValid(fieldIndex)) {
              final Object value = mergeableRecord.getCodeValue(fieldIndex);
              values.add(value);
            }
            for (final MergeOriginalRecord originalRecord : mergeableRecord.originalRecords) {
              if (originalRecord.isValid(fieldIndex) && !originalRecord.getFieldState(fieldIndex)
                .isMatchType(MergeFieldMatchType.END_FIELD_NOT_VALID)) {
                final Object originalValue = originalRecord.getCodeValue(fieldIndex);
                values.add(originalValue);
              }
            }
            for (final Object value : values) {
              final String menuTitle = "<html><b>Use:</b> <b color='red'>"
                + DataTypes.toString(value) + "</b></html>";
              menu.add(new RunnableAction(menuTitle, () -> {
                mergeableRecord.setOverrideValue(fieldIndex, value);
                mergeableRecord.setValue(fieldIndex, value);
                mergeableRecord.mergeValidateFields(false);
              }));
            }
            if (matchType == MergeFieldMatchType.OVERRIDDEN) {
              final String menuTitle = "<html><b color='red'>Remove Overrides</b></html>";
              menu.add(new RunnableAction(menuTitle, () -> {
                mergeableRecord.cleaOverrideValue(fieldIndex);
                mergeableRecord.mergeValidateFields(false);
              }));
            }
            return menu;
          }
          return null;
        }

        @Override
        public boolean isCellEditable(final int rowIndex) {

          if (super.isCellEditable(rowIndex)) {
            if (mergeableRecord.getMatchType(rowIndex) != MergeFieldMatchType.CANT_MERGE) {
              return true;
            }
          }
          return false;
        }
      } //
        .setCellEditor(table -> new RecordLayerTableCellEditor(table, this.layer) {
          /**
           *
           */
          private static final long serialVersionUID = 1L;

          @Override
          protected String getColumnFieldName(final int rowIndex, final int columnIndex) {
            return recordDefinition.getFieldName(rowIndex);
          }
        }) //
    );

    for (int i = 0; i < mergeableRecord.originalRecords.size(); i++) {
      final MergeOriginalRecord originalRecord = mergeableRecord.getOriginalRecord(i);
      model.addColumn(new RecordTableModelColumn("Record " + (i + 1), originalRecord, false));
    }

    mergeableRecord.tableModel = model;
    return model;
  }

  private void setMergedRecords(String errorMessage) {
    final List<LayerRecord> nonMergeableRecords = new ArrayList<>();

    for (final MergeableRecord mergeableRecord : this.recordMerger.records) {
      final List<MergeOriginalRecord> originalRecords = mergeableRecord.originalRecords;
      if (originalRecords.size() == 1) {
        for (final MergeOriginalRecord originalRecord : originalRecords) {
          nonMergeableRecords.add(originalRecord.originalRecord);
        }
      } else {
        newTable(mergeableRecord);
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

      this.mergedRecordsPanel.addClosableTab(nonMergeableRecords.size() + " Non-Mergeable", null,
        panel, () -> {
          if (this.mergedRecordsPanel.getTabCount() == 0) {
            setVisible(false);
          }
        });
    }
    SwingUtil.autoAdjustPosition(this);
  }

  @Override
  public String toString() {
    return getTitle();
  }

}
