package com.revolsys.util;

import java.util.UUID;

public class Uuid {

  public static UuidNamespace md5(final String namespace) {
    return new UuidNamespace(3, namespace);
  }

  public static UuidNamespace md5(final UUID namespace) {
    return new UuidNamespace(3, namespace);
  }

  public static UuidNamespace sha1(final String namespace) {
    return new UuidNamespace(5, namespace);
  }

  public static UuidNamespace sha1(final UUID namespace) {
    return new UuidNamespace(5, namespace);
  }

}
