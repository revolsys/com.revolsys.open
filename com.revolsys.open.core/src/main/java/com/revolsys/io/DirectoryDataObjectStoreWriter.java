package com.revolsys.io;

import javax.annotation.PreDestroy;

import com.revolsys.gis.data.model.DataObject;

public class DirectoryDataObjectStoreWriter extends AbstractWriter<DataObject> {

  private DirectoryDataObjectStore dataStore;

  public DirectoryDataObjectStoreWriter(
    DirectoryDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void write(DataObject object) {
    if (object != null) {
      try {
        boolean currentDataStore = object.getMetaData().getDataObjectStore() == dataStore;
        switch (object.getState()) {
          case New:
            dataStore.insert(object);
          break;
          case Modified:
            if (currentDataStore) {
              throw new UnsupportedOperationException();
            } else {
              dataStore.insert(object);
            }
          break;
          case Persisted:
            if (currentDataStore) {
              throw new UnsupportedOperationException();
            } else {
              dataStore.insert(object);
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

  @PreDestroy
  @Override
  public void close() {
    super.close();
    dataStore = null;
  }
}
