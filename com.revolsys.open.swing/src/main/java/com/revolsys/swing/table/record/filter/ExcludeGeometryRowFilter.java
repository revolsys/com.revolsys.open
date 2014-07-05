package com.revolsys.swing.table.record.filter;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.table.record.model.AbstractRecordTableModel;

public class ExcludeGeometryRowFilter extends RowFilter<TableModel, Integer> {

  @Override
  public boolean include(
    final Entry<? extends TableModel, ? extends Integer> entry) {
    final TableModel model = entry.getModel();
    if (model instanceof AbstractRecordTableModel) {
      final AbstractRecordTableModel dataObjectModel = (AbstractRecordTableModel)entry.getModel();
      final Integer identifier = entry.getIdentifier();
      final RecordDefinition metaData = dataObjectModel.getMetaData();
      final Class<?> clazz = metaData.getAttributeClass(identifier);
      if (Geometry.class.isAssignableFrom(clazz)) {
        return false;
      }
    }
    return true;
  }

}
