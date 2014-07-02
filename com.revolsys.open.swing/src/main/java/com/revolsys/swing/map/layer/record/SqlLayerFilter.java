package com.revolsys.swing.map.layer.record;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.UriTemplate;

public class SqlLayerFilter implements Filter<DataObject>, MapSerializer {
  private final String query;

  private Condition condition;

  private final AbstractDataObjectLayer layer;

  private boolean initialized;

  public SqlLayerFilter(final AbstractDataObjectLayer layer, final String query) {
    this.layer = layer;
    this.query = query;
  }

  @Override
  public boolean accept(final DataObject record) {
    final Condition condition = getCondition();
    if (condition == null) {
      return false;
    } else {
      if (condition.accept(record)) {
        return true;
      } else {
        return false;
      }
    }
  }

  private synchronized Condition getCondition() {
    if (condition == null) {
      if (!initialized) {
        final DataObjectMetaData metaData = layer.getMetaData();
        if (metaData != null) {
          initialized = true;
          try {
            final Properties properties = System.getProperties();
            final HashMap<String, Object> uriVariables = new HashMap<String, Object>();
            for (final Entry<Object, Object> entry : properties.entrySet()) {
              final String key = (String)entry.getKey();
              final Object value = entry.getValue();
              if (value != null) {
                uriVariables.put(key, value);
              }
            }

            final String query = new UriTemplate(this.query).expandString(uriVariables);
            condition = QueryValue.parseWhere(metaData, query);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error(
              "Invalid query: " + query, e);
          }
        }
      }
    }
    return condition;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "sqlFilter");
    map.put("query", query);
    return map;
  }

  @Override
  public String toString() {
    return query;
  }
}
