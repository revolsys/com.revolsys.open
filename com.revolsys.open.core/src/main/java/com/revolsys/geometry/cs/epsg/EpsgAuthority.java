package com.revolsys.geometry.cs.epsg;

import java.io.Serializable;

import com.revolsys.geometry.cs.Authority;

public class EpsgAuthority implements Authority, Serializable {
  private static final long serialVersionUID = 6255702398027894174L;

  private final int code;

  public EpsgAuthority(final int code) {
    this.code = code;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof EpsgAuthority) {
      final EpsgAuthority authority = (EpsgAuthority)object;
      return this.code == authority.code;
    } else if (object instanceof Authority) {
      final Authority authority = (Authority)object;
      if (!getName().equals(authority.getName())) {
        return false;
      } else if (!getCode().equals(authority.getCode())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public String getCode() {
    return String.valueOf(this.code);
  }

  @Override
  public String getName() {
    return "EPSG";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getName().hashCode();
    result = prime * result + this.code;
    return result;
  }

  @Override
  public String toString() {
    return getName() + ":" + this.code;
  }
}
