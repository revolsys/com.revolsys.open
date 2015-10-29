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
          case MODIFIED:
            this.recordStore.updateRecord(record);
          break;
          case PERSISTED:
            this.recordStore.updateRecord(record);
          break;
          case DELETED:
            this.recordStore.deleteRecord(record);
          break;
          default:
            this.recordStore.insertRecord(record);
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
