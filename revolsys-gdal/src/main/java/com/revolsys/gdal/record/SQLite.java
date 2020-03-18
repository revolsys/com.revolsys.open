package com.revolsys.gdal.record;

public class SQLite extends OgrRecordStoreFactory {
  public SQLite() {
    super("SQLite/SpatiaLite", "SQLite", "application/x-sqlite3", "sqlite");
  }

  @Override
  public boolean isDirectory() {
    return false;
  }
}
