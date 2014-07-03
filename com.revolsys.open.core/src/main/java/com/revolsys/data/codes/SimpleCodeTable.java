package com.revolsys.data.codes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.record.Record;

public class SimpleCodeTable extends AbstractCodeTable {

  public static CodeTable create(final String name, final Resource resource) {
    final SimpleCodeTable codeTable = new SimpleCodeTable(name);
    final DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
    try {
      for (final Record codeObject : reader) {
        final Identifier id = SingleIdentifier.create(
          codeObject.getValue(0));
        final List<Object> values = new ArrayList<Object>();
        final int attributeCount = codeObject.getMetaData().getAttributeCount();
        for (int i = 1; i < attributeCount; i++) {
          final Object value = codeObject.getValue(i);
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

  public SimpleCodeTable(final String name) {
    setName(name);
  }

  public void addValue(final Object id, final Object... values) {
    super.addValue(SingleIdentifier.create(id), values);
  }

  @Override
  public void addValue(final Identifier id, final Object... values) {
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
  protected Identifier loadId(final List<Object> values,
    final boolean createId) {
    this.index++;
    return SingleIdentifier.create(this.index);
  }

  @Override
  public void refresh() {
  }

}
