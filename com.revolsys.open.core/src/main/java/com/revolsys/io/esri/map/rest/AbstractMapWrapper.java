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

  public Map<String, Object> getValues() {
    return values;
  }

  public <T extends AbstractMapWrapper> List<T> getList(Class<T> clazz,
    String name) {
    List<T> objects = new ArrayList<T>();

    final List<Map<String, Object>> maps = getValue(name);
    if (maps != null) {
      for (Map<String, Object> map : maps) {
        try {
          T value = clazz.newInstance();
          value.setValues(map);
          objects.add(value);
        } catch (Throwable t) {
        }
      }
    }
    return objects;
  }

  public <T extends AbstractMapWrapper> T getObject(Class<T> clazz, String name) {
    Map<String, Object> values = getValue(name);
    if (values == null) {
      return null;
    } else {
      try {
        T value = clazz.newInstance();
        value.setValues(values);
        return value;
      } catch (Throwable t) {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String name) {
    final Map<String, Object> response = getValues();
    return (T)response.get(name);
  }

  protected void setValues(Map<String, Object> values) {
    this.values = values;
  }

  public Integer getIntValue(String name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.intValue();
    }
  }

  public Double getDoubleValue(String name) {
    final Number value = getValue(name);
    if (value == null) {
      return null;
    } else {
      return value.doubleValue();
    }
  }

  public GeometryFactory getSpatialReference() {
    Map<String, Object> spatialReference = getValue("spatialReference");
    if (spatialReference == null) {
      return GeometryFactory.getFactory();
    } else {
      Integer srid = CollectionUtil.getIntValue(spatialReference, "wkid");
      return GeometryFactory.getFactory(srid);
    }
  }

  public BoundingBox getBoundingBox(String name) {
    Map<String, Object> extent = getValue(name);
    if (extent == null) {
      return null;
    } else {
      Double minX = CollectionUtil.getDoubleValue(extent, "xmin");
      Double minY = CollectionUtil.getDoubleValue(extent, "ymin");
      Double maxX = CollectionUtil.getDoubleValue(extent, "xmax");
      Double maxY = CollectionUtil.getDoubleValue(extent, "ymax");

      GeometryFactory geometryFactory;
      Map<String, Object> spatialReference = (Map<String, Object>)extent.get("spatialReference");
      if (spatialReference == null) {
        geometryFactory = GeometryFactory.getFactory();
      } else {
        Integer srid = CollectionUtil.getIntValue(spatialReference, "wkid");
        geometryFactory = GeometryFactory.getFactory(srid);
      }
      return new BoundingBox(geometryFactory, minX, minY, maxX, maxY);
    }
  }

}
