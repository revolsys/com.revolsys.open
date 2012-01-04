package com.revolsys.io.page;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.revolsys.util.ExceptionUtil;

public class SerializableByteArraySerializer<T> implements PageValueManager<T> {
  @SuppressWarnings("unchecked")
  public <V extends T> V readFromByteArray(final byte[] bytes) {
    try {
      final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
      final ObjectInputStream in = new ObjectInputStream(bIn);
      return (V)in.readObject();
    } catch (final Exception e) {
      return (V)ExceptionUtil.throwUncheckedException(e);
    }
  }

  public <V extends T> V readFromPage(final Page page) {
    throw new UnsupportedOperationException();
  }

  public <V extends T> V removeFromPage(final Page page) {
    return readFromPage(page);
  }

  public byte[] writeToByteArray(final T value) {
    try {
      final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      final ObjectOutputStream out = new ObjectOutputStream(bOut);
      out.writeObject(value);
      out.close();
      return bOut.toByteArray();
    } catch (final Exception e) {
      return (byte[])ExceptionUtil.throwUncheckedException(e);
    }
  }

}
