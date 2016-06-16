package com.revolsys.swing.map.layer.record.table;

import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.font.TextAttribute;
import java.util.EventObject;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlightPredicate.AndHighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.highlighter.ColorHighlighter;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class RecordLayerTable extends RecordRowTable {
  private static final long serialVersionUID = 1L;

  public RecordLayerTable(final RecordLayerTableModel model) {
    super(model);
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setReorderingAllowed(false);
  }

  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  protected void addDeletedRecordHighlighter() {
    final RecordLayerTableModel model = getModel();
    final HighlightPredicate predicate = (renderer, adapter) -> {
      try {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        return model.isDeleted(rowIndex);
      } catch (final Throwable e) {
      }
      return false;
    };

    addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.newAlpha(WebColors.Pink, 127), WebColors.FireBrick, WebColors.LightCoral,
        WebColors.FireBrick));

    addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.Pink, WebColors.FireBrick, WebColors.Crimson, WebColors.White));

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
    final HighlightPredicate predicate = (renderer, adapter) -> {
      try {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        final LayerRecord record = model.getRecord(rowIndex);
        final AbstractRecordLayer layer = model.getLayer();
        return layer.isModified(record);
      } catch (final Throwable e) {
        return false;
      }
    };

    addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.newAlpha(WebColors.LimeGreen, 127), WebColors.Black,
        WebColors.newAlpha(WebColors.DarkGreen, 191), Color.WHITE));

    addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.LimeGreen, WebColors.Black, WebColors.DarkGreen, Color.WHITE));
  }

  @Override
  protected void addNewRecordHighlighter() {
    final RecordLayerTableModel model = getModel();
    final HighlightPredicate predicate = (renderer, adapter) -> {
      try {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        final LayerRecord record = model.getRecord(rowIndex);
        final AbstractRecordLayer layer = model.getLayer();
        return layer.isNew(record);
      } catch (final Throwable e) {
        return false;
      }
    };

    addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.newAlpha(WebColors.LightSkyBlue, 127), WebColors.Black,
        WebColors.newAlpha(WebColors.RoyalBlue, 191), Color.WHITE));

    addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.LightSkyBlue, WebColors.Black, WebColors.RoyalBlue, Color.WHITE));
  }

  public void copyFieldValue() {
    if (isEditingCurrentCell()) {
      final RecordTableCellEditor tableCellEditor = getTableCellEditor();
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

  public void cutFieldValue() {
    if (isEditingCurrentCell()) {
      final RecordTableCellEditor tableCellEditor = getTableCellEditor();
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
    final LayerRecord record = getRecord(row);
    LayerRecordMenu.setEventRecord(record);
    return super.editCellAt(row, column, e);
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
  protected RecordTableCellEditor newTableCellEditor() {
    return new RecordLayerTableCellEditor(this);
  }

  public void pasteFieldValue() {
    if (isEditingCurrentCell()) {
      final RecordTableCellEditor tableCellEditor = getTableCellEditor();
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
