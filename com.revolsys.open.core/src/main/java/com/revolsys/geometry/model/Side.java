package com.revolsys.geometry.model;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum Side {
  LEFT("Left"), RIGHT("Right");

  public static List<Side> VALUES = Lists.array(LEFT, RIGHT);

  public static boolean isLeft(final Side side) {
    return side == LEFT;
  }

  public static boolean isRight(final Side side) {
    return side == RIGHT;
  }

  public static Side opposite(final Side side) {
    if (side == LEFT) {
      return RIGHT;
    } else if (side == RIGHT) {
      return LEFT;
    } else {
      return null;
    }
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

  public boolean isLeft() {
    return this == LEFT;
  }

  public boolean isRight() {
    return this == RIGHT;
  }

  public Side opposite() {
    if (this == LEFT) {
      return RIGHT;
    } else {
      return LEFT;
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
