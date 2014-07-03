package com.revolsys.data.record.property;

import com.revolsys.data.record.schema.RecordDefinition;

public abstract class AbstractRecordDefinitionProperty implements
  RecordDefinitionProperty {
  private RecordDefinition recordDefinition;

  @Override
  public AbstractRecordDefinitionProperty clone() {
    try {
      final AbstractRecordDefinitionProperty clone = (AbstractRecordDefinitionProperty)super.clone();
      clone.recordDefinition = null;
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return recordDefinition;
  }

  @Override
  public void setRecordDefinition(final RecordDefinition metaData) {
    if (this.recordDefinition != null) {
      this.recordDefinition.setProperty(getPropertyName(), null);
    }
    this.recordDefinition = metaData;
    if (metaData != null) {
      metaData.setProperty(getPropertyName(), this);
    }
  }

  public String getTypePath() {
    return getRecordDefinition().getPath();
  }

}
