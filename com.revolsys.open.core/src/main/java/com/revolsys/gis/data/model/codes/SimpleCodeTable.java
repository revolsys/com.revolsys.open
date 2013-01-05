package com.revolsys.gis.data.model.codes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;

public class SimpleCodeTable extends AbstractCodeTable {

  public SimpleCodeTable(String name) {
    setName(name);
  }

  public static CodeTable create(String name, Resource resource) {
    SimpleCodeTable codeTable = new SimpleCodeTable(name);
    DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
    try {
      for (DataObject codeObject : reader) {
        Object id = codeObject.getValue(0);
        List<Object> values = new ArrayList<Object>();
        int attributeCount = codeObject.getMetaData().getAttributeCount();
        for (int i = 1; i < attributeCount; i++) {
          Object value = codeObject.getValue(i);
          values.add(value);
        }
        codeTable.addValue(id, values);
      }
    } finally {
      reader.close();
    }
    return codeTable;
  }

  private int index = 0;

  @Override
  public void addValue(final Object id, final Object... values) {
    super.addValue(id, values);
  }

  @Override
  public SimpleCodeTable clone() {
    return (SimpleCodeTable)super.clone();
  }

  @Override
  public String getIdAttributeName() {
    return getName();
  }

  @Override
  protected Object loadId(final List<Object> values, final boolean createId) {
    index++;
    return index;
  }

}
