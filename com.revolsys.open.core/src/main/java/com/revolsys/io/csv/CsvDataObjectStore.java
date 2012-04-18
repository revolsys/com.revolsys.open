package com.revolsys.io.csv;

import java.io.File;

import com.revolsys.io.AbstractDirectoryDataObjectStore;

public class CsvDataObjectStore extends AbstractDirectoryDataObjectStore {
  public CsvDataObjectStore() {
    setFileExtension("shp");
  }

  public CsvDataObjectStore(File directory) {
    this();
    setDirectory(directory);
  }
}
