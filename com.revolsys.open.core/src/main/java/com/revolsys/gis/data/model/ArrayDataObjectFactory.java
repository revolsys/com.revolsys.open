package com.revolsys.gis.data.model;

/**
 * The ArrayDataObjectFactory is an implementation of {@link DataObjectFactory}
 * for creating {@link ArrayRecord} instances.
 * 
 * @author Paul Austin
 * @see ArrayRecord
 */
public class ArrayDataObjectFactory implements DataObjectFactory {

  /**
   * Create an instance of ArrayRecord using the metadata
   * 
   * @param metaData The metadata used to create the instance.
   * @return The DataObject instance.
   */
  @Override
  public ArrayRecord createDataObject(final DataObjectMetaData metaData) {
    return new ArrayRecord(metaData);
  }
}
