package com.revolsys.gis.model.data.equals;

import java.util.Collection;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectEquals implements Equals<DataObject> {
  public static final String EXCLUDE_GEOMETRY = DataObjectEquals.class.getName()
    + ".excludeGeometry";

  public static final String EXCLUDE_ID = DataObjectEquals.class.getName()
    + ".excludeId";

  public static boolean equals(final DataObject object1,
    final DataObject object2, final String attributeName) {
    final Object value1 = object1.getValue(attributeName);
    final Object value2 = object2.getValue(attributeName);
    return EqualsRegistry.INSTANCE.equals(value1, value2);
  }

  private EqualsRegistry equalsRegistry;

  public boolean equals(final DataObject object1, final DataObject object2,
    final Collection<String> excludedAttributes) {
    if (object1 != null && object2 != null) {
      final DataObjectMetaData metaData1 = object1.getMetaData();
      final DataObjectMetaData metaData2 = object2.getMetaData();
      if (metaData1.getName().equals(metaData2.getName())) {
        if (metaData1.getAttributeCount() == metaData2.getAttributeCount()) {
          final int idIndex = metaData1.getIdAttributeIndex();
          final int geometryIndex = metaData1.getGeometryAttributeIndex();
          final int objectIdIndex = metaData1.getAttributeIndex("OBJECTID");
          for (int i = 0; i < metaData1.getAttributeCount(); i++) {
            final String name = metaData1.getAttributeName(i);
            if (excludedAttributes.contains(name)) {
            } else if (i == idIndex && excludedAttributes.contains(EXCLUDE_ID)) {
            } else if (i == geometryIndex
              && excludedAttributes.contains(EXCLUDE_GEOMETRY)) {
            } else if (i == objectIdIndex
              && excludedAttributes.contains(EXCLUDE_GEOMETRY)) {
            } else {
              final Object value1 = object1.getValue(i);
              final Object value2 = object2.getValue(i);
              if (!equalsRegistry.equals(value1, value2, excludedAttributes)) {
                return false;
              }
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }
}
