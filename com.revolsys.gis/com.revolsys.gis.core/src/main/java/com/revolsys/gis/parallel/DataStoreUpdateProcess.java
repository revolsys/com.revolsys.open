package com.revolsys.gis.parallel;

import javax.annotation.PreDestroy;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.io.Statistics;
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

public class DataStoreUpdateProcess extends BaseInProcess<DataObject> {
  /** The data store. */
  private DataObjectStore dataStore;

  /**
   * Construct a new DataStoreUpdateProcess.
   */
  public DataStoreUpdateProcess() {
  }

  /**
   * Get the data store.
   * 
   * @return The data store.
   */
  public DataObjectStore getDataStore() {
    return dataStore;
  }

  /**
   * Set the data store.
   * 
   * @param dataStore The data store.
   */
  public void setDataStore(
    final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * Process each object from the channel
   * 
   * @param in The input channel.
   * @param object The object to process.
   */
  @Override
  protected void process(
    final Channel<DataObject> in,
    final DataObject object) {
    final DataObjectState state = object.getState();
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
  
  @PreDestroy
  public void close() {
    dataStore.close();
  }
}
