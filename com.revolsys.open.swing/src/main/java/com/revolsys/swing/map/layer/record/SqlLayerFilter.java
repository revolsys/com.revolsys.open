package com.revolsys.swing.map.layer.record;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.Record;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.UriTemplate;

public class SqlLayerFilter implements Predicate<Record>, MapSerializer {
  private Condition condition;

  private boolean initialized;

  private final AbstractRecordLayer layer;

  private final String query;

  public SqlLayerFilter(final AbstractRecordLayer layer, final String query) {
    this.layer = layer;
    this.query = query;
  }

  public synchronized Condition getCondition() {
    if (this.condition == null) {
      if (!this.initialized) {
        final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
        if (recordDefinition != null) {
          this.initialized = true;
          try {
            final Properties properties = System.getProperties();
            final HashMap<String, Object> uriVariables = new HashMap<>();
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

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("type", "sqlFilter");
    map.put("query", this.query);
    return map;
  }

  @Override
  public String toString() {
    return this.query;
  }
}
