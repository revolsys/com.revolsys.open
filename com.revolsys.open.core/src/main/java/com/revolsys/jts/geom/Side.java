package com.revolsys.jts.geom;

public enum Side {
  LEFT("Left"), RIGHT("Right");
  public static boolean isLeft(final Side side) {
    return side == LEFT;
  }

  public static boolean isRight(final Side side) {
    return side == RIGHT;
  }

  private String name;

  private char letter;

  private Side(final String name) {
    this.name = name;
    this.letter = name.charAt(0);
  }

  public char getLetter() {
    return this.letter;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
