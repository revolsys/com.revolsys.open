package com.revolsys.record;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.revolsys.record.schema.RecordDefinition;

public abstract class BaseRecord extends AbstractRecord implements Serializable {
  private static final long serialVersionUID = 1L;

  private transient RecordDefinition recordDefinition;

  private RecordState state = RecordState.INITIALIZING;

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
  public boolean isState(final RecordState state) {
    return this.state == state;
  }

  @Override
  public RecordState setState(final RecordState state) {
    final RecordState oldState = this.getState();
    this.state = state;
    return oldState;
  }

  private void writeObject(final ObjectOutputStream oos) throws IOException {
    // oos.writeInt(this.recordDefinition.getInstanceId());
    oos.defaultWriteObject();
  }
}
