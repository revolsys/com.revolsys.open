package com.revolsys.swing.map.layer.dataobject.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.table.BaseJxTable;
import com.vividsolutions.jts.geom.Geometry;

public class MergedRecordsTable extends BaseJxTable {
  public MergedRecordsTable(final DataObjectLayer layer) {
    super(new MergedRecordsTableModel(layer));
    setSortable(false);

    final DataObjectMetaData metaData = layer.getMetaData();
    tableHeader.setReorderingAllowed(false);

    final JTableHeader tableHeader = getTableHeader();
    final MergedRecordsTableCellRenderer cellRenderer = new MergedRecordsTableCellRenderer();
    final MergedRecordsTableModel model = getTableModel();
    final List<TableColumn> removeColumns = new ArrayList<TableColumn>();
    final TableColumnModel columnModel = getColumnModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);
      if (i == 0) {
        column.setCellRenderer(cellRenderer);
      } else {
        final Class<?> attributeClass = metaData.getAttributeClass(i - 1);
        if (Geometry.class.isAssignableFrom(attributeClass)) {
          removeColumns.add(column);
        } else {
          column.setCellRenderer(cellRenderer);
        }
      }
    }
    for (final TableColumn column : removeColumns) {
      removeColumn(column);
    }
    // tableHeader.addMouseListener(this);
    model.setTable(this);
    addHighlighter(new ColorHighlighter(WebColors.White, WebColors.Black,
      WebColors.Blue, WebColors.White));

    addHighlighter(new ColorHighlighter(HighlightPredicate.ODD,
      WebColors.LightGray, null, WebColors.Navy, null));
    addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN,
      WebColors.White, null, WebColors.Blue, null));

    MergedValuePredicate.add(this);
    MergedObjectPredicate.add(this);
  }

  public MergedRecordsTableModel getTableModel() {
    return (MergedRecordsTableModel)getModel();
  }

  @Override
  protected void initializeColumnPreferredWidth(final TableColumn column) {
    super.initializeColumnPreferredWidth(column);

    final MergedRecordsTableModel model = (MergedRecordsTableModel)getModel();
    final DataObjectMetaData metaData = model.getMetaData();
    final int viewIndex = column.getModelIndex();

    if (viewIndex > 0) {
      final String attributeName = model.getAttributeName(viewIndex);
      final Attribute attribute = metaData.getAttribute(attributeName);

      Integer columnWidth = attribute.getProperty("tableColumnWidth");
      final String columnName = attribute.getTitle();
      if (columnWidth == null) {
        columnWidth = attribute.getMaxStringLength() * 7;
        columnWidth = Math.min(columnWidth, 200);
        attribute.setProperty("tableColumnWidth", columnWidth);
      }
      column.setMinWidth(columnName.length() * 7 + 15);
      column.setPreferredWidth(columnWidth);
    } else {
      column.setMinWidth(70);
      column.setPreferredWidth(70);
    }
  }

}
