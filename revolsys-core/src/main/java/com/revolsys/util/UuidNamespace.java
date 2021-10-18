package com.revolsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.jeometry.common.exception.Exceptions;

public class UuidNamespace {

  private static UUID fromBytes(final byte[] data) {
    long msb = 0;
    long lsb = 0;
    assert data.length >= 16;
    for (int i = 0; i < 8; i++) {
      msb = msb << 8 | data[i] & 0xff;
    }
    for (int i = 8; i < 16; i++) {
      lsb = lsb << 8 | data[i] & 0xff;
    }
    return new UUID(msb, lsb);
  }

  private final byte[] bytes = new byte[16];

  private final UUID namespace;

  public UuidNamespace(final String namespace) {
    this(UUID.fromString(namespace));
  }

  public UuidNamespace(final UUID namespace) {
    this.namespace = namespace;
    final byte[] bytes = this.bytes;
    final long msb = namespace.getMostSignificantBits();
    final long lsb = namespace.getLeastSignificantBits();
    for (int i = 0; i < 8; i++) {
      bytes[i] = (byte)(msb >> (7 - i) * 8 & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      bytes[i] = (byte)(lsb >> (15 - i) * 8 & 0xff);
    }

  }

  public UUID newV5(final byte[] name) {
    try {
      final MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(this.bytes);
      md.update(name);
      final byte[] sha1Bytes = md.digest();
      sha1Bytes[6] &= 0x0f; /* clear version */
      sha1Bytes[6] |= 0x50; /* set to version 5 */
      sha1Bytes[8] &= 0x3f; /* clear variant */
      sha1Bytes[8] |= 0x80; /* set to IETF variant */
      return fromBytes(sha1Bytes);
    } catch (final NoSuchAlgorithmException e) {
      throw Exceptions.wrap(e);
    }

  }

  public UUID newV5(final String name) {
    return newV5(name.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String toString() {
    return this.namespace.toString();
  }

}
