package com.revolsys.swing.map.layer.record;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.UriTemplate;

public class RecordDefinitionSqlFilter implements Predicate<Record>, MapSerializer {
  private Condition condition;

  private boolean initialized;

  private final RecordDefinitionProxy recordDefinitionProxy;

  private final String query;

  public RecordDefinitionSqlFilter(final RecordDefinitionProxy recordDefinitionProxy,
    final String query) {
    this.recordDefinitionProxy = recordDefinitionProxy;
    this.query = query;
  }

  public synchronized Condition getCondition() {
    if (this.condition == null) {
      if (!this.initialized) {
        final RecordDefinition recordDefinition = this.recordDefinitionProxy.getRecordDefinition();
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
            Logs.error(this, "Invalid query: " + this.query, e);
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
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addTypeToMap(map, "sqlFilter");
    map.put("query", this.query);
    return map;
  }

  @Override
  public String toString() {
    return this.query;
  }
}
