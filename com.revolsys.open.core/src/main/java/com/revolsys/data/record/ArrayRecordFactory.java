package com.revolsys.data.record;

import com.revolsys.data.record.schema.RecordDefinition;

/**
 * The ArrayRecordFactory is an implementation of {@link RecordFactory}
 * for creating {@link ArrayRecord} instances.
 * 
 * @author Paul Austin
 * @see ArrayRecord
 */
public class ArrayRecordFactory implements RecordFactory {

  /**
   * Create an instance of ArrayRecord using the metadata
   * 
   * @param recordDefinition The metadata used to create the instance.
   * @return The Record instance.
   */
  @Override
  public ArrayRecord createRecord(final RecordDefinition recordDefinition) {
    return new ArrayRecord(recordDefinition);
  }
}
