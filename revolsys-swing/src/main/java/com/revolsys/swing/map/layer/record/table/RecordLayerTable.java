package com.revolsys.swing.map.layer.record.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.util.EventObject;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.event.TableColumnModelExtListener;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.jeometry.common.awt.WebColors;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.highlighter.OddEvenColorHighlighter;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class RecordLayerTable extends RecordRowTable {
  private final class ColumnWidthListener implements TableColumnModelExtListener {

    @Override
    public void columnAdded(final TableColumnModelEvent e) {
    }

    @Override
    public void columnMarginChanged(final ChangeEvent e) {
    }

    @Override
    public void columnMoved(final TableColumnModelEvent e) {
    }

    @Override
    public void columnPropertyChange(final PropertyChangeEvent event) {
      final Object source = event.getSource();
      if (source instanceof TableColumnExt) {
        if ("width".equals(event.getPropertyName())) {
          final AbstractRecordLayer layer = getLayer();
          if (layer != null) {
            if (!isInitializingColumnWidths()) {
              final TableColumnExt column = (TableColumnExt)source;
              final int columnIndex = column.getModelIndex();
              final String fieldName = getColumnFieldName(columnIndex);
              final int width = column.getWidth();
              layer.setFieldColumnWidth(fieldName, width);
            }
          }
        }
      }

    }

    @Override
    public void columnRemoved(final TableColumnModelEvent e) {
    }

    @Override
    public void columnSelectionChanged(final ListSelectionEvent e) {
    }
  }

  private static final long serialVersionUID = 1L;

  public RecordLayerTable(final RecordLayerTableModel model) {
    super(model);
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setReorderingAllowed(false);

    final TableColumnModelExt columnModel = (TableColumnModelExt)getColumnModel();
    columnModel.addColumnModelListener(new ColumnWidthListener());
    addNotQueryRecordHighlighter();
  }

  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected void addDeletedRecordHighlighter() {
    final RecordLayerTableModel model = getModel();
    final OddEvenColorHighlighter colorHighlighter = addColorHighlighter(
      (rowIndex, columnIndex) -> {
        try {
          final LayerRecord record = model.getRecord(rowIndex);
          final AbstractRecordLayer layer = model.getLayer();
          return layer.isDeleted(record);
        } catch (final Throwable e) {
          return false;
        }
      }, WebColors.LightPink, WebColors.Crimson);

    final HighlightPredicate predicate = colorHighlighter.getHighlightPredicate();
    final Font tableFont = getFont();
    final Map<TextAttribute, Object> fontAttributes = (Map)tableFont.getAttributes();
    fontAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
    final Font font = new Font(fontAttributes);
    final FontHighlighter fontHighlighter = new FontHighlighter(predicate, font);
    addHighlighter(fontHighlighter);
  }

  @Override
  protected void addModifiedRecordHighlighter() {
    final RecordLayerTableModel model = getModel();
    addColorHighlighter((rowIndex, columnIndex) -> {
      try {
        final LayerRecord record = model.getRecord(rowIndex);
        final AbstractRecordLayer layer = model.getLayer();
        return layer.isModified(record);
      } catch (final Throwable e) {
        return false;
      }
    }, WebColors.newAlpha(WebColors.LimeGreen, 127), WebColors.newAlpha(WebColors.DarkGreen, 191));
  }

  @Override
  protected void addNewRecordHighlighter() {
    final RecordLayerTableModel model = getModel();
    addColorHighlighter((rowIndex, columnIndex) -> {
      try {
        final LayerRecord record = model.getRecord(rowIndex);
        final AbstractRecordLayer layer = model.getLayer();
        return layer.isNew(record);
      } catch (final Throwable e) {
        return false;
      }
    }, WebColors.LightSkyBlue, WebColors.CornflowerBlue);
  }

  private void addNotQueryRecordHighlighter() {
    final RecordLayerTableModel model = getModel();
    final HighlightPredicate predicate = newPredicateModelRowColumn((rowIndex, columnIndex) -> {
      try {
        final Query filterQuery = getModel().getFilterQuery();
        final LayerRecord record = model.getRecord(rowIndex);
        final Condition whereCondition = filterQuery.getWhereCondition();
        if (whereCondition.test(record)) {
          return false;
        } else {
          whereCondition.test(record);
          return true;
        }
      } catch (final Throwable e) {
        return false;
      }
    });

    final Font tableFont = getFont();
    @SuppressWarnings({
      "rawtypes", "unchecked"
    })
    final Map<TextAttribute, Object> fontAttributes = (Map)tableFont.getAttributes();
    fontAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
    final Font font = new Font(fontAttributes);
    final OddEvenColorHighlighter highlighter = new OddEvenColorHighlighter(predicate,
      WebColors.LemonChiffon, WebColors.Gold) {

      @Override
      protected Component doHighlight(final Component component, final ComponentAdapter adapter) {
        super.doHighlight(component, adapter);
        component.setFont(font);
        return component;
      }
    } //
    // .setForeground(WebColors.Gold)//
    // .setForegroundSelected(WebColors.LightYellow)
    ;
    addHighlighter(highlighter);
  }

  @Override
  public void copyFieldValue() {
    if (isEditingCurrentCell()) {
      final BaseTableCellEditor tableCellEditor = getTableCellEditor();
      final JComponent editorComponent = tableCellEditor.getEditorComponent();
      SwingUtil.dndCopy(editorComponent);
    } else {
      final RecordRowTableModel model = getTableModel();
      final int row = TablePanel.getEventRow();
      final int column = TablePanel.getEventColumn();
      final Object value = model.getValueAt(row, column);
      if (value != null) {
        final String copyValue;
        if (value instanceof Geometry) {
          final Geometry geometry = (Geometry)value;
          copyValue = geometry.toEwkt();
        } else {
          copyValue = model.toDisplayValue(row, column, value);
        }
        final StringSelection transferable = new StringSelection(copyValue);
        ClipboardUtil.setContents(transferable);
      }
    }
  }

  @Override
  protected RowSorter<? extends TableModel> createDefaultRowSorter() {
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
      return new RecordLayerTableRowSorter(layer, model);
    }
    return null;
  }

  @Override
  public void cutFieldValue() {
    if (isEditingCurrentCell()) {
      final BaseTableCellEditor tableCellEditor = getTableCellEditor();
      final JComponent editorComponent = tableCellEditor.getEditorComponent();
      SwingUtil.dndCut(editorComponent);
    } else {
      copyFieldValue();
      final RecordRowTableModel model = getTableModel();
      final int row = TablePanel.getEventRow();
      final int column = TablePanel.getEventColumn();
      model.setValueAt(null, row, column);
    }
  }

  @Override
  public boolean editCellAt(final int row, final int column, final EventObject e) {
    if (e == null || e instanceof MouseEvent && !((MouseEvent)e).isShiftDown()) {
      final LayerRecord record = getRecord(row);
      LayerRecordMenu.setEventRecord(record);
      return super.editCellAt(row, column, e);
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends AbstractRecordLayer> V getLayer() {
    final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
    if (model == null) {
      return null;
    } else {
      return (V)model.getLayer();
    }
  }

  @Override
  public RecordLayerTableModel getModel() {
    return (RecordLayerTableModel)super.getModel();
  }

  @Override
  public ListSelectionModel getSelectionModel() {
    final RecordLayerTableModel model = getModel();
    final ListSelectionModel selectionModel = model.getSelectionModel();
    if (selectionModel == null) {
      return super.getSelectionModel();
    } else {
      return selectionModel;
    }
  }

  @Override
  protected void initializeColumnPreferredWidth(final TableColumn column) {
    final int columnIndex = column.getModelIndex();
    final String fieldName = getColumnFieldName(columnIndex);
    if (fieldName != null) {
      final AbstractRecordLayer layer = getLayer();
      final int width = layer.getFieldColumnWidth(fieldName);
      if (width >= 0) {
        column.setWidth(width);
        column.setPreferredWidth(width);
      } else {
        super.initializeColumnPreferredWidth(column);
      }
    }
  }

  @Override
  protected RecordTableCellEditor newTableCellEditor() {
    return new RecordLayerTableCellEditor(this);
  }

  @Override
  public void pasteFieldValue() {
    if (isEditingCurrentCell()) {
      final BaseTableCellEditor tableCellEditor = getTableCellEditor();
      final JComponent editorComponent = tableCellEditor.getEditorComponent();
      SwingUtil.dndPaste(editorComponent);
    } else {
      try {
        final Transferable clipboard = ClipboardUtil.getContents();
        final Object value = clipboard.getTransferData(DataFlavor.stringFlavor);
        final RecordRowTableModel model = getTableModel();
        final int row = TablePanel.getEventRow();
        final int column = TablePanel.getEventColumn();
        model.setValueAt(value, row, column);
      } catch (final Throwable e) {
      }
    }
  }

  @Override
  public void setSelectionModel(ListSelectionModel newModel) {
    if (newModel == null) {
      newModel = createDefaultSelectionModel();
    }
    super.setSelectionModel(newModel);
  }

  @Override
  protected void tableChangedDo(final TableModelEvent event) {
    final RecordLayerTableModel model = getModel();
    if (model.isSortable()) {
      setSortable(true);
    } else {
      setSortable(false);
    }
    final RowFilter<RecordRowTableModel, Integer> rowFilter = model.getRowFilter();
    final boolean filterChanged = getRowFilter() != rowFilter;
    if (filterChanged) {
      setRowFilter(null);
    }
    super.tableChangedDo(event);
    if (filterChanged) {
      setRowFilter(rowFilter);
    }
    repaint();
  }
}
