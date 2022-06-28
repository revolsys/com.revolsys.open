package com.revolsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Longs;

public class UuidNamespace {

  public static UUID toUuid(final byte[] bytes) {
    final long l1 = Longs.toLong(bytes, 0);
    final long l2 = Longs.toLong(bytes, 8);
    return new UUID(l1, l2);
  }

  public static UUID toUuid(final int type, final byte[] bytes) {
    bytes[6] &= 0x0f; // clear version
    bytes[6] |= type << 4; // set to version
    bytes[8] &= 0x3f; // clear variant
    bytes[8] |= 0x80; // set to IETF variant
    return toUuid(bytes);
  }

  private final byte[] namespaceBytes = new byte[16];

  private final UUID namespace;

  private int type;

  private String algorithm;

  UuidNamespace(final int type, final String namespace) {
    this(type, UUID.fromString(namespace));
  }

  UuidNamespace(final int type, final UUID namespace) {
    if (type == 3) {
      this.algorithm = "MD5";
    } else if (type == 5) {
      this.algorithm = "SHA-1";
    }
    this.namespace = namespace;
    final byte[] bytes = this.namespaceBytes;
    final long msb = namespace.getMostSignificantBits();
    final long lsb = namespace.getLeastSignificantBits();
    for (int i = 0; i < 8; i++) {
      bytes[i] = (byte)(msb >> (7 - i) * 8 & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      bytes[i] = (byte)(lsb >> (15 - i) * 8 & 0xff);
    }
  }

  public UuidBuilder builder() {
    try {
      final MessageDigest digester = MessageDigest.getInstance(this.algorithm);
      digester.update(this.namespaceBytes);
      return new UuidBuilder(this.type, digester);
    } catch (final NoSuchAlgorithmException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public String toString() {
    return this.namespace.toString();
  }

  public UUID uuid(final byte[] name) {
    try {
      final MessageDigest digester = MessageDigest.getInstance(this.algorithm);
      digester.update(this.namespaceBytes);
      digester.update(name);
      final byte[] digest = digester.digest();
      return UuidNamespace.toUuid(this.type, digest);
    } catch (final NoSuchAlgorithmException e) {
      throw Exceptions.wrap(e);
    }
  }

  public UUID uuid(final Object name) {
    if (name == null) {
      return uuid("null");
    } else if (name instanceof byte[]) {
      return uuid((byte[])name);
    } else {
      return uuid(name.toString());

    }

  }

  public UUID uuid(final Object... values) {
    final UuidBuilder builder = builder();
    boolean first = true;
    for (final Object value : values) {
      if (first) {
        first = false;
      } else {
        builder.separator();
      }
      builder.append(value);

    }
    return builder.build();
  }

  public UUID uuid(final String name) {
    final byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
    return uuid(bytes);
  }

  public String uuidString(final byte[] name) {
    return uuid(name).toString();
  }

  public String uuidString(final Object name) {
    return uuid(name).toString();
  }

  public String uuidString(final String name) {
    return uuid(name).toString();
  }

}
