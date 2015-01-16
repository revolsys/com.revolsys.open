package com.revolsys.io.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileSystemConnectionManager {
  public static FileSystemConnectionManager get() {
    return INSTANCE;
  }

  private static FileSystemConnectionManager INSTANCE = new FileSystemConnectionManager();

  public List<File> getFileSystems() {
    return Arrays.asList(File.listRoots());
  }

  @Override
  public String toString() {
    return "File Systems";
  }
}
