package com.revolsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.UuidIdentifier;
import org.jeometry.common.data.type.DataTypes;

public class UuidBuilder {

  private final MessageDigest digester;

  private final int type;

  UuidBuilder(final int type, final MessageDigest digester) {
    this.type = type;
    this.digester = digester;
  }

  public UuidBuilder append(final byte[] bytes) {
    if (bytes != null) {
      this.digester.update(bytes);
    }
    return this;
  }

  public UuidBuilder append(final CharSequence string) {
    if (string != null) {
      append(string.toString());
    }
    return this;
  }

  public UuidBuilder append(final Object value) {
    if (value == null) {
      append("null");
    } else if (value instanceof String) {
      append((String)value);
    } else if (value != null) {
      final String string = DataTypes.toString(value);
      append(string);
    }
    return this;
  }

  public UuidBuilder append(final String string) {
    if (string != null) {
      final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
      append(bytes);
    }
    return this;
  }

  public UUID build() {
    final byte[] digest = this.digester.digest();
    return UuidNamespace.toUuid(this.type, digest);
  }

  public UuidIdentifier buildIdentifier() {
    final UUID uuid = build();
    return Identifier.newIdentifier(uuid);
  }

  public Identifier newStringIdentifier() {
    final String string = toString();
    return Identifier.newIdentifier(string);
  }

  public UuidBuilder separator() {
    return append(""); // UNICODE Group Separator
                        // https://unicode-table.com/en/001D/
  }

  @Override
  public String toString() {
    final UUID uuid = build();
    return uuid.toString();
  }
}
