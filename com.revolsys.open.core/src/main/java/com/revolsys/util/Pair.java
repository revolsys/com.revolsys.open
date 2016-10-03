package com.revolsys.util;

public class Pair<A, B> {
  public static <A, B> Pair<A, B> newPair(final A value1, final B value2) {
    return new Pair<>(value1, value2);
  }

  private A value1;

  private B value2;

  public Pair(final A value1, final B value2) {
    super();
    this.value1 = value1;
    this.value2 = value2;
  }

  public A getValue1() {
    return this.value1;
  }

  public B getValue2() {
    return this.value2;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    if (this.value1 != null) {
      hash = this.value1.hashCode();
    }
    if (this.value2 != null) {
      hash = hash << 8 + this.value2.hashCode();
    }
    return super.hashCode();
  }

  public void setValue1(final A value1) {
    this.value1 = value1;
  }

  public void setValue2(final B value2) {
    this.value2 = value2;
  }

  @Override
  public String toString() {
    return this.value1 + ", " + this.value2;
  }
}
