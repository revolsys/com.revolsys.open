package com.revolsys.record;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.revolsys.record.schema.RecordDefinition;

public abstract class BaseRecord extends AbstractRecord implements Serializable {
  private static final long serialVersionUID = 1L;

  private transient RecordDefinition recordDefinition;

  protected RecordState state = RecordState.Initializing;

  public BaseRecord(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public RecordState getState() {
    return this.state;
  }

  @Override
  public void setState(final RecordState state) {
    this.state = state;
  }

  private void writeObject(final ObjectOutputStream oos) throws IOException {
    oos.writeInt(this.recordDefinition.getInstanceId());
    oos.defaultWriteObject();
  }
}
