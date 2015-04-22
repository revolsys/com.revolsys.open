package com.revolsys.format.saif.util;

import java.io.IOException;

public interface OsnConverter {
  Object read(final OsnIterator iterator);

  void write(final OsnSerializer serializer, Object object) throws IOException;
}
