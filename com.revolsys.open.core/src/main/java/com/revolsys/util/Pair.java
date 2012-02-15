package com.revolsys.util;

public class Pair<A, B> {
  public static <A, B> Pair<A, B> create(final A value1, final B value2) {
    return new Pair<A, B>(value1, value2);
  }

  private A value1;

  private B value2;

  public Pair(final A value1, final B value2) {
    super();
    this.value1 = value1;
    this.value2 = value2;
  }

  public A getValue1() {
    return value1;
  }

  public B getValue2() {
    return value2;
  }

  public void setValue1(final A value1) {
    this.value1 = value1;
  }

  public void setValue2(final B value2) {
    this.value2 = value2;
  }

}
