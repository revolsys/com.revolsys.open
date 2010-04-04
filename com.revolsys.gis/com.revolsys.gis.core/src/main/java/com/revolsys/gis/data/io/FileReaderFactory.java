package com.revolsys.gis.data.io;

import java.io.File;

public interface FileReaderFactory<T> {
  Reader<T> createReader(
    File file);
}
