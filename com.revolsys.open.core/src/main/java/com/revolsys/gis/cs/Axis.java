package com.revolsys.gis.cs;

import java.io.Serializable;

public class Axis implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 5463484439488623454L;

  private final String direction;

  private final String name;

  public Axis(final String name, final String direction) {
    this.name = name;
    this.direction = direction;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof Axis) {
      final Axis axis = (Axis)object;
      if (!name.equals(axis.name)) {
        return false;
      } else if (!direction.equals(axis.direction)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public String getDirection() {
    return direction;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + direction.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return name;
  }
}
