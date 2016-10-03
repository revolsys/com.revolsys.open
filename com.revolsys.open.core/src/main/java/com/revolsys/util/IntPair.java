package com.revolsys.util;

public class IntPair {

  private int value1;

  private int value2;

  public IntPair(final int value1, final int value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      final IntPair other = (IntPair)obj;
      if (this.value1 != other.value1) {
        return false;
      } else if (this.value2 != other.value2) {
        return false;
      }
      return true;
    }
  }

  public int getValue1() {
    return this.value1;
  }

  public int getValue2() {
    return this.value2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.value1;
    result = prime * result + this.value2;
    return result;
  }

  public void setValue1(final int value1) {
    this.value1 = value1;
  }

  public void setValue2(final int value2) {
    this.value2 = value2;
  }

  @Override
  public String toString() {
    return this.value1 + ", " + this.value2;
  }
}
