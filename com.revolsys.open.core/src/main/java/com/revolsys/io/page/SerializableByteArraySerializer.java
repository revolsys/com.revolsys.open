package com.revolsys.io.page;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.revolsys.util.ExceptionUtil;

public class SerializableByteArraySerializer<T> implements PageValueManager<T> {
  @SuppressWarnings("unchecked")
  public <V extends T> V getValue(final byte[] bytes) {
    try {
      final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
      final ObjectInputStream in = new ObjectInputStream(bIn);
      return (V)in.readObject();
    } catch (final Exception e) {
      return (V)ExceptionUtil.throwUncheckedException(e);
    }
  }

  public byte[] getBytes(Page page) {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  public <V extends T> V readFromPage(final Page page) {
    byte[] bytes = getBytes(page);
    return getValue(bytes);
 }


  public byte[] getBytes(final T value) {
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
