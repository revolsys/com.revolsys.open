package com.revolsys.record.io.format.esri.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.properties.BaseObjectWithProperties;

public class AbstractMapWrapper extends BaseObjectWithProperties {
  public static BoundingBox newBoundingBox(final MapEx properties, final String name) {
    final MapEx extent = properties.getValue(name);
    if (extent == null) {
      return null;
    } else {
      final double minX = extent.getDouble("xmin");
      final double minY = extent.getDouble("ymin");
      final double maxX = extent.getDouble("xmax");
      final double maxY = extent.getDouble("ymax");

      final GeometryFactory geometryFactory = newGeometryFactory(extent, "spatialReference");
      return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
    }
  }

  @SuppressWarnings("unchecked")
  public static GeometryFactory newGeometryFactory(final Map<String, ? extends Object> properties,
    final String fieldName) {
    final Map<String, Object> spatialReference = (Map<String, Object>)properties.get(fieldName);
    if (spatialReference == null) {
      return GeometryFactory.DEFAULT;
    } else {
      Integer srid = Maps.getInteger(spatialReference, "latestWkid");
      if (srid == null) {
        srid = Maps.getInteger(spatialReference, "wkid");
        if (srid == 102100) {
          srid = 3857;
        } else if (srid == 102190) {
          srid = 3005;
        }
      }
      return GeometryFactory.floating3(srid);
    }
  }

  public static <T extends AbstractMapWrapper> List<T> newList(final Class<T> clazz,
    final MapEx properties, final String name) {
    final List<T> objects = new ArrayList<T>();

    final List<MapEx> maps = properties.getValue(name);
    if (maps != null) {
      for (final MapEx map : maps) {
        try {
          final T value = clazz.newInstance();
          value.setProperties(map);
          objects.add(value);
        } catch (final Throwable t) {
          t.printStackTrace();
        }
      }
    }
    return objects;
  }

  public static <T extends AbstractMapWrapper> T newObject(final Class<T> clazz,
    final MapEx properties, final String name) {
    final MapEx values = properties.getValue(name);
    if (values == null) {
      return null;
    } else {
      try {
        final T value = clazz.newInstance();
        value.setProperties(values);
        return value;
      } catch (final Throwable t) {
        t.printStackTrace();
        return null;
      }
    }
  }

  private final Object resfreshSync = new Object();

  private boolean initialized = false;

  public AbstractMapWrapper() {
  }

  public Object getResfreshSync() {
    return this.resfreshSync;
  }

  public final void refresh() {
    synchronized (this.resfreshSync) {
      refreshDo();
    }
  }

  protected void refreshDo() {
  }

  public final void refreshIfNeeded() {
    synchronized (this.resfreshSync) {
      if (!this.initialized) {
        this.initialized = true;
        refresh();
      }
    }
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    name = name.substring(name.indexOf('.') + 1);
    return name + getProperties();
  }
}
