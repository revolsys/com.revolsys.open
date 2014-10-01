package com.revolsys.gis.parallel;

import javax.annotation.PreDestroy;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInProcess;

/**
 * <p>
 * The RecordStoreUpdateProcess process reads each object from the input channel
 * and updates the object in the record store based on the object's state.
 * </p>
 * <p>
 * The following actions will be performed based on the state of the object.
 * </p>
 * <dl>
 * <dt>New</dt>
 * <dd>Insert the object into the record store.</dd>
 * <dt>Persisted</dt>
 * <dd>No action performed.</dd>
 * <dt>Modified</dt>
 * <dd>Update the object in the record store.</dd>
 * <dt>Deleted</dt>
 * <dd>Delete the object from the record store.</dd>
 * </dl>
 */

public class RecordStoreUpdateProcess extends BaseInProcess<Record> {
  /** The record store. */
  private RecordStore recordStore;

  /**
   * Construct a new RecordStoreUpdateProcess.
   */
  public RecordStoreUpdateProcess() {
  }

  @PreDestroy
  public void close() {
    this.recordStore.close();
  }

  /**
   * Get the record store.
   *
   * @return The record store.
   */
  public RecordStore getRecordStore() {
    return this.recordStore;
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
        this.recordStore.insert(object);
        break;
      case Persisted:
        break;
      case Modified:
        this.recordStore.update(object);
        break;
      case Deleted:
        this.recordStore.delete(object);
        break;
      default:
        break;
    }
  }

  /**
   * Set the record store.
   *
   * @param recordStore The record store.
   */
  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }
}
