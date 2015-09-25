package com.revolsys.record.io.format.directory;

import javax.annotation.PreDestroy;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;

public class DirectoryRecordStoreWriter extends AbstractRecordWriter {

  private DirectoryRecordStore recordStore;

  public DirectoryRecordStoreWriter(final DirectoryRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @PreDestroy
  @Override
  public void close() {
    super.close();
    this.recordStore = null;
  }

  @Override
  public void write(final Record record) {
    if (record != null) {
      try {
        final RecordState state = record.getState();
        switch (state) {
          case Modified:
            this.recordStore.update(record);
          break;
          case Persisted:
            this.recordStore.update(record);
          break;
          case Deleted:
            this.recordStore.delete(record);
          break;
          default:
            this.recordStore.insert(record);
          break;
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
