package com.revolsys.swing.table.dataobject.filter;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.table.dataobject.model.AbstractDataObjectTableModel;
import com.vividsolutions.jts.geom.Geometry;

public class ExcludeGeometryRowFilter extends RowFilter<TableModel, Integer> {

  @Override
  public boolean include(
    final Entry<? extends TableModel, ? extends Integer> entry) {
    final TableModel model = entry.getModel();
    if (model instanceof AbstractDataObjectTableModel) {
      final AbstractDataObjectTableModel dataObjectModel = (AbstractDataObjectTableModel)entry.getModel();
      final Integer identifier = entry.getIdentifier();
      final DataObjectMetaData metaData = dataObjectModel.getMetaData();
      final Class<?> clazz = metaData.getAttributeClass(identifier);
      if (Geometry.class.isAssignableFrom(clazz)) {
        return false;
      }
    }
    return true;
  }

}
