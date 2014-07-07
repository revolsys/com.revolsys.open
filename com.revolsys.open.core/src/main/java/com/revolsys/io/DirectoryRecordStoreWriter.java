package com.revolsys.io;

import javax.annotation.PreDestroy;

import com.revolsys.data.record.Record;

public class DirectoryRecordStoreWriter extends AbstractWriter<Record> {

  private DirectoryRecordStore recordStore;

  public DirectoryRecordStoreWriter(final DirectoryRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @PreDestroy
  @Override
  public void close() {
    super.close();
    recordStore = null;
  }

  @Override
  public void write(final Record object) {
    if (object != null) {
      try {
        final boolean currentRecordStore = object.getRecordDefinition()
          .getRecordStore() == recordStore;
        switch (object.getState()) {
          case New:
            recordStore.insert(object);
          break;
          case Modified:
            if (currentRecordStore) {
              throw new UnsupportedOperationException();
            } else {
              recordStore.insert(object);
            }
          break;
          case Persisted:
            if (currentRecordStore) {
              throw new UnsupportedOperationException();
            } else {
              recordStore.insert(object);
            }
          case Deleted:
            throw new UnsupportedOperationException();
          default:
            throw new IllegalStateException("State not known");
        }
      } catch (final RuntimeException e) {
        throw e;
      } catch (final Error e) {
        throw e;
      } catch (final Exception e) {
        throw new RuntimeException("Unable to write", e);
      }
    }
  }
}
