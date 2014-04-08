package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jts.GeometryComparator;
import com.revolsys.util.CompareUtil;
import com.revolsys.jts.geom.Geometry;

public class DataObjectGeometryComparitor implements Comparator<DataObject> {

  private static final GeometryComparator GEOMETRYC_COMPARATOR = new GeometryComparator();

  private boolean decending = false;

  public DataObjectGeometryComparitor() {
  }

  public DataObjectGeometryComparitor(final boolean decending) {
    this.decending = decending;
  }

  @Override
  public int compare(final DataObject object1, final DataObject object2) {
    if (object1 == object2) {
      return 0;
    } else {
      final Geometry geometry1 = object1.getGeometryValue();
      final Geometry geometry2 = object2.getGeometryValue();
      int compare = CompareUtil.compare(GEOMETRYC_COMPARATOR, geometry1,
        geometry2);
      if (compare == 0) {
        compare = geometry1.compareTo(geometry2);
        if (compare == 0) {
          final Object id1 = object1.getIdValue();
          final Object id2 = object2.getIdValue();
          compare = CompareUtil.compare(id1, id2);
          if (compare == 0) {
            final DataObjectMetaData metaData1 = object1.getMetaData();
            final DataObjectMetaData metaData2 = object2.getMetaData();
            final Set<String> attributeNames = new LinkedHashSet<String>();
            attributeNames.addAll(metaData1.getAttributeNames());
            attributeNames.addAll(metaData2.getAttributeNames());
            compare = compareAttributes(object1, object2, attributeNames);
          }
        }
      }
      if (decending) {
        return -compare;
      } else {
        return compare;
      }
    }
  }

  public int compareAttributes(final DataObject object1,
    final DataObject object2, final Set<String> attributeNames) {
    for (final String attributeName : attributeNames) {
      final Object value1 = object1.getValue(attributeName);
      final Object value2 = object2.getValue(attributeName);
      final int compare = CompareUtil.compare(value1, value2);
      if (compare != 0) {
        return compare;
      }
    }
    return 0;
  }

}
