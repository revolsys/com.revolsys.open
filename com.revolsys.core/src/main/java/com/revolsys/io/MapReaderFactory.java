package com.revolsys.io;

import java.io.InputStream;
import java.io.Reader;

import org.springframework.core.io.Resource;

public interface MapReaderFactory extends IoFactory {
  MapReader createMapReader(
    final Resource resource);
  
  MapReader createMapReader(
    final InputStream in);
 
  MapReader createMapReader(
    final Reader in);
}
