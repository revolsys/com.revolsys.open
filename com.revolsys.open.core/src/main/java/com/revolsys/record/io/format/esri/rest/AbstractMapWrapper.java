package com.revolsys.record.io.format.esri.rest;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

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
      return geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
    }
  }

  public static GeometryFactory newGeometryFactory(final MapEx properties, final String fieldName) {
    final MapEx spatialReference = properties.getValue(fieldName);
    if (spatialReference == null) {
      return GeometryFactory.DEFAULT;
    } else {
      Integer srid = spatialReference.getInteger("latestWkid");
      if (srid == null) {
        srid = spatialReference.getInteger("wkid");
        if (srid == null) {
          final String wkt = spatialReference.getString("wkt");
          if (Property.hasValue(wkt)) {
            return GeometryFactory.getFactory(wkt);
          } else {
            return GeometryFactory.DEFAULT;
          }
        } else if (srid == 102100) {
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
    final List<T> objects = new ArrayList<>();

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

  private boolean hasError = false;

  public AbstractMapWrapper() {
  }

  public boolean isHasError() {
    return this.hasError;
  }

  public final void refresh() {
    synchronized (this.resfreshSync) {
      try (
        BaseCloseable noCache = FileResponseCache.disable()) {
        refreshDo();
        this.hasError = false;
      } catch (final Throwable e) {
        this.hasError = true;
        throw Exceptions.wrap("Unable to initialize: " + this, e);
      }
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

  protected void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    name = name.substring(name.indexOf('.') + 1);
    return name + getProperties();
  }
}
