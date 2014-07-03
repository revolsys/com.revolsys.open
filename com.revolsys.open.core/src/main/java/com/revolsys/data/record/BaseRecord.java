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

  /** The metaData defining the object type. */
  private transient RecordDefinition metaData;

  protected RecordState state = RecordState.Initalizing;

  /**
   * Construct a new empty BaseRecord using the metaData.
   *
   * @param metaData The metaData defining the object type.
   */
  public BaseRecord(final RecordDefinition metaData) {
    this.metaData = metaData;
  }

  /**
   * Get the metd data describing the Record and it's attributes.
   *
   * @return The meta data.
   */
  @Override
  public RecordDefinition getMetaData() {
    return this.metaData;
  }

  @Override
  public RecordState getState() {
    return this.state;
  }

  private void readObject(final ObjectInputStream ois)
      throws ClassNotFoundException, IOException {
    final int metaDataInstanceId = ois.readInt();
    this.metaData = RecordDefinitionImpl.getMetaData(metaDataInstanceId);
    ois.defaultReadObject();
  }

  @Override
  public void setState(final RecordState state) {
    this.state = state;
  }

  private void writeObject(final ObjectOutputStream oos) throws IOException {
    oos.writeInt(this.metaData.getInstanceId());
    oos.defaultWriteObject();
  }
}
