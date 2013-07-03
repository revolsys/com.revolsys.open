package com.revolsys.io;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FolderConnectionManager {
  private static FolderConnectionManager INSTANCE = new FolderConnectionManager();

  public static FolderConnectionManager get() {
    return INSTANCE;
  }

  public List<File> getFileSystems() {
    return Arrays.asList(File.listRoots());
  }

  @Override
  public String toString() {
    return "File Systems";
  }
}
