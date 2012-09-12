package com.revolsys.gis.converter.process;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.gis.data.model.DataObject;

public class SetValues extends
  AbstractSourceToTargetProcess<DataObject, DataObject> {
  private Map<String, ? extends Object> values = Collections.emptyMap();

  public SetValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  public Map<String, ? extends Object> getValues() {
    return values;
  }

  @Override
  public void process(final DataObject source, final DataObject target) {
    for (final Entry<String, ? extends Object> entry : values.entrySet()) {
      final String name = entry.getKey();
      final Object value = entry.getValue();
      if (value != null) {
        target.setValueByPath(name, value);
      }
    }
  }

  public void setValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "set" + values;
  }
}
