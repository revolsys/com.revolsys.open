package com.revolsys.io.esri.map.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.util.CollectionUtil;

public class AbstractMapWrapper {

  private Map<String, Object> values;

  public AbstractMapWrapper() {
  }

  public BoundingBox getBoundingBox(final String name) {
    final Map<String, Object> extent = getValue(name);
    if (extent == null) {
      return null;
    } else {
      final Double minX = CollectionUtil.getDoubleValue(extent, "xmin");
      final Double minY = CollectionUtil.getDoubleValue(extent, "ymin");
      final Double maxX = CollectionUtil.getDoubleValue(extent, "xmax");
      final Double maxY = CollectionUtil.getDoubleValue(extent, "ymax");

      GeometryFactory geometryFactory;
      final Map<String, Object> spatialReference = (Map<String, Object>)extent.get("spatialReference");
      if (spatialReference == null) {
        geometryFactory = GeometryFactory.getFactory();
      } else {
        Integer srid = CollectionUtil.getInteger(spatialReference, "wkid");
        if (srid == 102100) {
          srid = 3857;
        }
        geometryFactory = GeometryFactory.getFactory(srid);
      }
      return new BoundingBox(geometryFactory, minX, minY, maxX, maxY);
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

  public Integer getIntValue(final String name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.intValue();
    }
  }

  public <T extends AbstractMapWrapper> List<T> getList(final Class<T> clazz,
    final String name) {
    final List<T> objects = new ArrayList<T>();

    final List<Map<String, Object>> maps = getValue(name);
    if (maps != null) {
      for (final Map<String, Object> map : maps) {
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

  public <T extends AbstractMapWrapper> T getObject(final Class<T> clazz,
    final String name) {
    final Map<String, Object> values = getValue(name);
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
    if (spatialReference == null) {
      return GeometryFactory.getFactory();
    } else {
      Integer srid = CollectionUtil.getInteger(spatialReference, "wkid");
      if (srid == 102100) {
        srid = 3857;
      }
      return GeometryFactory.getFactory(srid);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String name) {
    final Map<String, Object> response = getValues();
    return (T)response.get(name);
  }

  public Map<String, Object> getValues() {
    return values;
  }

  protected void setValues(final Map<String, Object> values) {
    this.values = values;
  }

}
