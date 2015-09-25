package com.revolsys.geometry.test.old.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtil {
  public static Object deserialize(final byte[] data) throws IOException, ClassNotFoundException {
    final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
    return in.readObject();
  }

  public static byte[] serialize(final Object obj) throws IOException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(obj);
    out.close();
    final byte[] treeBytes = bos.toByteArray();
    return treeBytes;
  }

}
