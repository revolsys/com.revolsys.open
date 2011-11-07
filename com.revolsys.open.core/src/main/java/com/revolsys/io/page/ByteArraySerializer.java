package com.revolsys.io.page;

import java.lang.reflect.Method;

import com.revolsys.util.JavaBeanUtil;

public class ByteArraySerializer {

  public static ByteArraySerializer DOUBLE = new ByteArraySerializer("Double");

  public static byte[] writeByteToByteArray(final Byte b) {
    return new byte[] {
      b
    };
  }

  public static byte[] writeDoubleToByteArray(final Double d) {
    final long l = Double.doubleToLongBits(d);
    return writeLongToByteArray(l);
  }

  public static byte[] writeFloatToByteArray(final Float f) {
    final int i = Float.floatToIntBits(f);
    return writeIntToByteArray(i);
  }

  public static byte[] writeIntToByteArray(final Integer i) {
    final byte b1 = (byte)((i >>> 24) & 0xFF);
    final byte b2 = (byte)((i >>> 16) & 0xFF);
    final byte b3 = (byte)((i >>> 8) & 0xFF);
    final byte b4 = (byte)((i >>> 0) & 0xFF);
    return new byte[] {
      b1, b2, b3, b4
    };
  }

  public static byte[] writeLongToByteArray(final Long l) {
    final byte b1 = (byte)(l >>> 56);
    final byte b2 = (byte)(l >>> 48);
    final byte b3 = (byte)(l >>> 40);
    final byte b4 = (byte)(l >>> 32);
    final byte b5 = (byte)(l >>> 24);
    final byte b6 = (byte)(l >>> 16);
    final byte b7 = (byte)(l >>> 8);
    final byte b8 = (byte)(l >>> 0);
    return new byte[] {
      b1, b2, b3, b4, b5, b6, b7, b8
    };
  }

  public static byte[] writeShortToByteArray(final Short s) {
    final byte b1 = (byte)((s >>> 8) & 0xFF);
    final byte b2 = (byte)((s >>> 0) & 0xFF);
    return new byte[] {
      b1, b2,
    };
  }

  private Method byteArrayWriteMethod;

  private Method byteArrayReadMethod;

  private Method pageReadMethod;

  public static ByteArraySerializer BYTE = new ByteArraySerializer("Byte");

  public static ByteArraySerializer SHORT = new ByteArraySerializer("Short");

  public static ByteArraySerializer INT = new ByteArraySerializer("Int");

  public static ByteArraySerializer LONG = new ByteArraySerializer("Long");

  public static ByteArraySerializer FLOAT = new ByteArraySerializer("Float");

  public static Byte readByteFromByteArray(final byte[] bytes) {
    return bytes[0];
  }

  public static Byte readByteFromPage(final Page page) {
    return page.readByte();
  }

  public static Double readDoubleFromByteArray(final byte[] bytes) {
    final long l = readLongFromByteArray(bytes);
    return Double.longBitsToDouble(l);
  }

  public static Double readDoubleFromPage(final Page page) {
    return page.readDouble();
  }

  public static Float readFloatFromByteArray(final byte[] bytes) {
    final int i = readIntFromByteArray(bytes);
    return Float.intBitsToFloat(i);
  }

  public static Float readFloatFromPage(final Page page) {
    return page.readFloat();
  }

  public static Integer readIntFromByteArray(final byte[] bytes) {
    final int b1 = bytes[0] & 0xFF;
    final int b2 = bytes[1] & 0xFF;
    final int b3 = bytes[2] & 0xFF;
    final int b4 = bytes[3] & 0xFF;
    return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
  }

  public static Integer readIntFromPage(final Page page) {
    return page.readInt();
  }

  public static Long readLongFromByteArray(final byte[] bytes) {
    final int b1 = bytes[0] & 0xFF;
    final int b2 = bytes[1] & 0xFF;
    final int b3 = bytes[2] & 0xFF;
    final int b4 = bytes[3] & 0xFF;
    final int b5 = bytes[4] & 0xFF;
    final int b6 = bytes[5] & 0xFF;
    final int b7 = bytes[6] & 0xFF;
    final int b8 = bytes[7] & 0xFF;
    return (((long)b1 << 56) + ((long)(b2 & 255) << 48)
      + ((long)(b3 & 255) << 40) + ((long)(b4 & 255) << 32)
      + ((long)(b5 & 255) << 24) + ((b6 & 255) << 16) + ((b7 & 255) << 8) + ((b8 & 255) << 0));
  }

  public static Short readShortFromPage(final Page page) {
    return page.readShort();
  }

  public static Short readShortFromByteArray(final byte[] bytes) {
    final int b1 = bytes[0] & 0xFF;
    final int b2 = bytes[1] & 0xFF;
    return (short)((b1 << 8) + (b2 << 0));
  }

  public static Long readLongFromPage(final Page page) {
    return page.readLong();
  }

  protected ByteArraySerializer() {
  }

  protected ByteArraySerializer(final String typeName) {
    this("write" + typeName + "ToByteArray", "read" + typeName
      + "FromByteArray", "read" + typeName + "FromPage");
  }

  protected ByteArraySerializer(final String byteArrayWriteMethodName,
    final String byteArrayReadMethodName, final String pageReadMethodName) {
    for (final Method method : getClass().getMethods()) {
      if (method.getName().equals(byteArrayWriteMethodName)) {
        byteArrayWriteMethod = method;
      }
      if (method.getName().equals(byteArrayReadMethodName)) {
        byteArrayReadMethod = method;
      }
      if (method.getName().equals(pageReadMethodName)) {
        pageReadMethod = method;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T readFromByteArray(final byte[] bytes) {
    return (T)JavaBeanUtil.invokeMethod(byteArrayReadMethod, getClass(), bytes);
  }

  @SuppressWarnings("unchecked")
  public <T> T readFromPage(final Page page) {
    return (T)JavaBeanUtil.invokeMethod(pageReadMethod, getClass(), page);
  }

  public byte[] writeToByteArray(final Object v) {
    return JavaBeanUtil.invokeMethod(byteArrayWriteMethod, getClass(), v);
  }
}
