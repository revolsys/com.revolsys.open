package com.revolsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.util.Hex;

public class Sha1Builder {
  private MessageDigest digester;

  public Sha1Builder() {
    try {
      this.digester = MessageDigest.getInstance("SHA-1");
    } catch (final NoSuchAlgorithmException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  public Sha1Builder(final String namespace) {
    this();
    append(namespace);
  }

  public Sha1Builder append(final byte[] bytes) {
    if (bytes != null) {
      this.digester.update(bytes);
    }
    return this;
  }

  public Sha1Builder append(final Object value) {
    if (value instanceof String) {
      append((String)value);
    } else if (value != null) {
      final String string = DataTypes.toString(value);
      append(string);
    }
    return this;
  }

  public Sha1Builder append(final String string) {
    final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
    return append(bytes);
  }

  public String newHex() {
    final byte[] digest = this.digester.digest();
    return Hex.toHex(digest);
  }

  public Identifier newStringIdentifier() {
    final String string = toString();
    return Identifier.newIdentifier(string);
  }

  @Override
  public String toString() {
    return newHex();
  }
}
