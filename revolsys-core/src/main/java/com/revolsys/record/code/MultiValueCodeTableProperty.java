package com.revolsys.record.code;

import java.util.Map;

import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.schema.RecordDefinition;

public class MultiValueCodeTableProperty extends MultiValueRecordStoreCodeTable
  implements RecordDefinitionProperty {

  public static final String PROPERTY_NAME = MultiValueRecordStoreCodeTable.class.getName();

  public MultiValueCodeTableProperty() {
    super();
  }

  public MultiValueCodeTableProperty(final Map<String, ? extends Object> config) {
    super();
    setProperties(config);
  }

  @Override
  protected void clearRecordDefinition() {
    super.clearRecordDefinition();
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      recordDefinition.setProperty(getPropertyName(), null);
    }
  }

  @Override
  public MultiValueCodeTableProperty clone() {
    return (MultiValueCodeTableProperty)super.clone();
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  @Override
  protected void setRecordDefinitionDo(final RecordDefinition recordDefinition) {
    recordDefinition.setProperty(getPropertyName(), this);
    recordDefinition.setProperty("codeTable", this.getCodeTable());
    getRecordStore().addCodeTable(this.getCodeTable());
  }
}
