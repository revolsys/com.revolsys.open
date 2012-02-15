package com.revolsys.gis.data.io;

import java.io.File;

import com.revolsys.io.Reader;

public interface FileReaderFactory<T> {
  Reader<T> createReader(File file);
}
