package com.revolsys.gis.parallel;

import javax.annotation.PreDestroy;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInProcess;

/**
 * <p>
 * The DataStoreUpdateProcess process reads each object from the input channel
 * and updates the object in the data store based on the object's state.
 * </p>
 * <p>
 * The following actions will be performed based on the state of the object.
 * </p>
 * <dl>
 * <dt>New</dt>
 * <dd>Insert the object into the data store.</dd>
 * <dt>Persisted</dt>
 * <dd>No action performed.</dd>
 * <dt>Modified</dt>
 * <dd>Update the object in the data store.</dd>
 * <dt>Deleted</dt>
 * <dd>Delete the object from the data store.</dd>
 * </dl>
 */

public class DataStoreUpdateProcess extends BaseInProcess<Record> {
  /** The data store. */
  private RecordStore dataStore;

  /**
   * Construct a new DataStoreUpdateProcess.
   */
  public DataStoreUpdateProcess() {
  }

  @PreDestroy
  public void close() {
    dataStore.close();
  }

  /**
   * Get the data store.
   * 
   * @return The data store.
   */
  public RecordStore getDataStore() {
    return dataStore;
  }

  /**
   * Process each object from the channel
   * 
   * @param in The input channel.
   * @param object The object to process.
   */
  @Override
  protected void process(final Channel<Record> in, final Record object) {
    final RecordState state = object.getState();
    switch (state) {
      case New:
        dataStore.insert(object);
      break;
      case Persisted:
      break;
      case Modified:
        dataStore.update(object);
      break;
      case Deleted:
        dataStore.delete(object);
      break;
      default:
      break;
    }
  }

  /**
   * Set the data store.
   * 
   * @param dataStore The data store.
   */
  public void setDataStore(final RecordStore dataStore) {
    this.dataStore = dataStore;
  }
}
