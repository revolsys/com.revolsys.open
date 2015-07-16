package com.revolsys.swing.map.layer.record;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.revolsys.data.query.Condition;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import java.util.function.Predicate;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.UriTemplate;

public class SqlLayerFilter implements Predicate<Record>, MapSerializer {
  private final String query;

  private Condition condition;

  private final AbstractRecordLayer layer;

  private boolean initialized;

  public SqlLayerFilter(final AbstractRecordLayer layer, final String query) {
    this.layer = layer;
    this.query = query;
  }

  @Override
  public boolean test(final Record record) {
    final Condition condition = getCondition();
    if (condition == null) {
      return false;
    } else {
      if (condition.test(record)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public synchronized Condition getCondition() {
    if (this.condition == null) {
      if (!this.initialized) {
        final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
        if (recordDefinition != null) {
          this.initialized = true;
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
            this.condition = QueryValue.parseWhere(recordDefinition, query);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error("Invalid query: " + this.query, e);
          }
        }
      }
    }
    return this.condition;
  }

  public String getQuery() {
    return this.query;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "sqlFilter");
    map.put("query", this.query);
    return map;
  }

  @Override
  public String toString() {
    return this.query;
  }
}
