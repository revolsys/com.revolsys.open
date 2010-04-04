package com.revolsys.io;

import java.util.Set;

public interface IoFactory {
  String getFileExtension(
    String mediaType);

  Set<String> getFileExtensions();

  String getMediaType(
    String fileExtension);

  Set<String> getMediaTypes();
  
  String getName();
}
