package com.revolsys.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.revolsys.io.FileUtil;

public class Compress {
  public static byte[] deflate(final byte[] bytes) {
    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try {
      final Deflater delator = new Deflater(Deflater.BEST_COMPRESSION, true);
      final OutputStream compressOut = new DeflaterOutputStream(byteOut, delator);
      compressOut.write(bytes);
      compressOut.close();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to deflate", e);
    }
    return byteOut.toByteArray();
  }

  public static byte[] deflate(final String text) {
    try {
      final byte[] bytes = text.getBytes("UTF-8");
      return deflate(bytes);
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 not supported", e);
    }
  }

  public static String deflateBase64(final String text) {
    final byte[] bytes = deflate(text);
    return Base64.encodeBytesNoWrap(bytes);
  }

  public static String inflate(final byte[] bytes) {
    final ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    return inflate(byteIn);
  }

  public static String inflate(final InputStream in) {
    final Inflater delator = new Inflater(true);
    final InputStream compressIn = new InflaterInputStream(in, delator);
    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try {
      FileUtil.copy(compressIn, byteOut);
      return new String(byteOut.toByteArray(), "UTF-8");
    } catch (final IOException e) {
      throw new RuntimeException("Unable to inflate", e);
    } finally {
      FileUtil.closeSilent(compressIn);
      FileUtil.closeSilent(byteOut);
    }
  }

  public static String inflateBase64(final String text) {
    final byte[] bytes = Base64.decodeBytesNoWrap(text);
    return inflate(bytes);
  }

}
