package com.revolsys.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

  public static MessageDigest getMessageDigest() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (final NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 Digest not found", e);
    }
  }

  public static byte[] md5(final byte[] data) {
    final MessageDigest messageDigest = getMessageDigest();
    return messageDigest.digest(data);
  }

  public static byte[] md5(final InputStream data) throws IOException {
    final MessageDigest digest = getMessageDigest();
    final int bufferSize = 1024;
    final byte[] buffer = new byte[bufferSize];
    int read = data.read(buffer, 0, bufferSize);

    while (read > -1) {
      digest.update(buffer, 0, read);
      read = data.read(buffer, 0, bufferSize);
    }

    return digest.digest();
  }

  public static byte[] md5(final String data) {
    try {
      return md5(data.getBytes("UTF-8"));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 encoding not found", e);
    }
  }

  public static String md5Hex(final byte[] data) {
    final byte[] md5 = md5(data);
    return Hex.toHex(md5);
  }

  public static String md5Hex(final InputStream data) throws IOException {
    final byte[] md5 = md5(data);
    return Hex.toHex(md5);
  }

  public static String md5Hex(final String data) {
    final byte[] md5 = md5(data);
    return Hex.toHex(md5);
  }
}
