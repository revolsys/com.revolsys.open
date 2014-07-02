package com.revolsys.gis.data.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class BaseRecord extends AbstractRecord implements Serializable {
  /** Seialization version */
  private static final long serialVersionUID = 2704226494490082708L;

  /** The metaData defining the object type. */
  private transient DataObjectMetaData metaData;

  protected DataObjectState state = DataObjectState.Initalizing;

  /**
   * Construct a new empty BaseRecord using the metaData.
   *
   * @param metaData The metaData defining the object type.
   */
  public BaseRecord(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  /**
   * Get the metd data describing the DataObject and it's attributes.
   *
   * @return The meta data.
   */
  @Override
  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  @Override
  public DataObjectState getState() {
    return this.state;
  }

  private void readObject(final ObjectInputStream ois)
      throws ClassNotFoundException, IOException {
    final int metaDataInstanceId = ois.readInt();
    this.metaData = DataObjectMetaDataImpl.getMetaData(metaDataInstanceId);
    ois.defaultReadObject();
  }

  @Override
  public void setState(final DataObjectState state) {
    this.state = state;
  }

  private void writeObject(final ObjectOutputStream oos) throws IOException {
    oos.writeInt(this.metaData.getInstanceId());
    oos.defaultWriteObject();
  }
}
