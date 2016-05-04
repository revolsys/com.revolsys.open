package com.revolsys.record.io.format.esri.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.util.Property;

public class AbstractMapWrapper {
  private MapEx values;

  public AbstractMapWrapper() {
  }

  public BoundingBox getBoundingBox(final String name) {
    final Map<String, Object> extent = getValue(name);
    if (extent == null) {
      return null;
    } else {
      final Double minX = Maps.getDoubleValue(extent, "xmin");
      final Double minY = Maps.getDoubleValue(extent, "ymin");
      final Double maxX = Maps.getDoubleValue(extent, "xmax");
      final Double maxY = Maps.getDoubleValue(extent, "ymax");

      GeometryFactory geometryFactory;
      @SuppressWarnings("unchecked")
      final Map<String, Object> spatialReference = (Map<String, Object>)extent
        .get("spatialReference");
      geometryFactory = getGeometryFactory(spatialReference);
      return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
    }
  }

  public Double getDoubleValue(final String name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  public GeometryFactory getGeometryFactory(final Map<String, Object> spatialReference) {
    GeometryFactory geometryFactory;
    if (spatialReference == null) {
      geometryFactory = GeometryFactory.DEFAULT;
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
      geometryFactory = GeometryFactory.floating3(srid);
    }
    return geometryFactory;
  }

  public Integer getIntValue(final String name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.intValue();
    }
  }

  public <T extends AbstractMapWrapper> List<T> getList(final Class<T> clazz, final String name) {
    final List<T> objects = new ArrayList<T>();

    final List<MapEx> maps = getValue(name);
    if (maps != null) {
      for (final MapEx map : maps) {
        try {
          final T value = clazz.newInstance();
          value.setValues(map);
          objects.add(value);
        } catch (final Throwable t) {
        }
      }
    }
    return objects;
  }

  public <T extends AbstractMapWrapper> T getObject(final Class<T> clazz, final String name) {
    final MapEx values = getValue(name);
    if (values == null) {
      return null;
    } else {
      try {
        final T value = clazz.newInstance();
        value.setValues(values);
        return value;
      } catch (final Throwable t) {
        return null;
      }
    }
  }

  public GeometryFactory getSpatialReference() {
    final Map<String, Object> spatialReference = getValue("spatialReference");
    return getGeometryFactory(spatialReference);
  }

  public <T> T getValue(final String name) {
    final MapEx response = getValues();
    return response.getValue(name);
  }

  public MapEx getValues() {
    return this.values;
  }

  public boolean hasValue(final String name) {
    final MapEx response = getValues();
    return Property.hasValue(response.get(name));
  }

  protected void setValues(final MapEx values) {
    this.values = values;
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    name = name.substring(name.indexOf('.') + 1);
    return name + getValues();
  }
}
