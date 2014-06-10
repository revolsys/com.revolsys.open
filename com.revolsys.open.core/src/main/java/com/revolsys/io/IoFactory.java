package com.revolsys.io;

import java.util.List;
import java.util.Set;

public interface IoFactory {
  String getFileExtension(String mediaType);

  List<String> getFileExtensions();

  String getMediaType(String fileExtension);

  Set<String> getMediaTypes();

  String getName();

  boolean isAvailable();
}
