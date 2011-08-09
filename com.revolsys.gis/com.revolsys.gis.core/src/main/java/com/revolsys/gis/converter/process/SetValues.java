package com.revolsys.gis.converter.process;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;

public class SetValues implements SourceToTargetProcess<DataObject, DataObject> {
  private Map<String, ? extends Object> values = Collections.emptyMap();

  public SetValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  public Map<String, ? extends Object> getValues() {
    return values;
  }

  public void process(final DataObject source, final DataObject target) {
    for (final Entry<String, ? extends Object> entry : values.entrySet()) {
      final String name = entry.getKey();
      final Object value = entry.getValue();
      if (value != null) {
        final DataObjectMetaData targetMetaData = target.getMetaData();
        final DataObjectStore targetDataObjectStore = targetMetaData.getDataObjectStore();
        if (targetDataObjectStore == null) {
          target.setValue(name, value);
        } else {
          final CodeTable codeTable = targetDataObjectStore.getCodeTableByColumn(name);
          if (codeTable == null) {
            target.setValue(name, value);
          } else if (value instanceof Number) {
            target.setValue(name, value);
          } else {
            final Object codeId = codeTable.getId(value);
            target.setValue(name, codeId);
          }
        }
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
