package com.revolsys.swing.table.dataobject;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.vividsolutions.jts.geom.Geometry;

public class ExcludeGeometryRowFilter extends RowFilter<TableModel, Integer> {

  @Override
  public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
    TableModel model = entry.getModel();
    if (model instanceof AbstractDataObjectTableModel) {
      AbstractDataObjectTableModel dataObjectModel = (AbstractDataObjectTableModel)entry.getModel();
      Integer identifier = entry.getIdentifier();
      DataObjectMetaData metaData= dataObjectModel.getMetaData();
      Class<?> clazz = metaData.getAttributeClass(identifier);
      if (Geometry.class.isAssignableFrom(clazz)) {
        return false;
      }
    }
    return true;
  }

}
