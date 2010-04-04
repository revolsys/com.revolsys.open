package com.revolsys.gis.cs;

public class Authority {
  private final String code;

  private final String name;

  public Authority(
    final String name,
    final int code) {
    this(name, String.valueOf(code));
  }

  public Authority(
    final String name,
    final String code) {
    this.name = name;
    this.code = code;
  }

  @Override
  public boolean equals(
    final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof Authority) {
      final Authority authority = (Authority)object;
      if (!name.equals(authority.name)) {
        return false;
      } else if (!code.equals(authority.code)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + code.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return name + ":" + code;
  }
}
