package com.revolsys.data.record;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;

public abstract class BaseRecord extends AbstractRecord implements Serializable {
  /** Seialization version */
  private static final long serialVersionUID = 2704226494490082708L;

  /** The recordDefinition defining the object type. */
  private transient RecordDefinition recordDefinition;

  protected RecordState state = RecordState.Initalizing;

  /**
   * Construct a new empty BaseRecord using the recordDefinition.
   *
   * @param recordDefinition The recordDefinition defining the object type.
   */
  public BaseRecord(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  /**
   * Get the metd data describing the Record and it's attributes.
   *
   * @return The meta data.
   */
  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public RecordState getState() {
    return this.state;
  }

  private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
    final int recordDefinitionInstanceId = ois.readInt();
    this.recordDefinition = RecordDefinitionImpl.getRecordDefinition(recordDefinitionInstanceId);
    ois.defaultReadObject();
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
