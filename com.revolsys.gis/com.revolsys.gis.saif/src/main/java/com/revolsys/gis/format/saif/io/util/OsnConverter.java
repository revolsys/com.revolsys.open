package com.revolsys.gis.format.saif.io.util;

import java.io.IOException;

public interface OsnConverter {
  Object read(
    final OsnIterator iterator);

  void write(
    final OsnSerializer serializer,
    Object object)
    throws IOException;
}
