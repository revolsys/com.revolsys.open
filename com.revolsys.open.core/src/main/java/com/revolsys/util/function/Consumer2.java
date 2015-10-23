package com.revolsys.util.function;

@FunctionalInterface
public interface Consumer2<P1, P2> {
  void accept(P1 parameter1, P2 parameter2);
}
