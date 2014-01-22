package com.revolsys.gis.data.model;

/**
 * The ArrayDataObjectFactory is an implementation of {@link DataObjectFactory}
 * for creating {@link ArrayDataObject} instances.
 * 
 * @author Paul Austin
 * @see ArrayDataObject
 */
public class ArrayDataObjectFactory implements DataObjectFactory {

  /**
   * Create an instance of ArrayDataObject using the metadata
   * 
   * @param metaData The metadata used to create the instance.
   * @return The DataObject instance.
   */
  @Override
  public ArrayDataObject createDataObject(final DataObjectMetaData metaData) {
    return new ArrayDataObject(metaData);
  }
}
