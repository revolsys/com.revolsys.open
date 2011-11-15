package com.revolsys.io;

import java.util.Map;

import org.springframework.core.io.Resource;

public interface MapReaderFactory extends IoFactory {
  Reader<Map<String, Object>> createMapReader(final Resource resource);
}
