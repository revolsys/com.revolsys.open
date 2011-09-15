package com.revolsys.gis.converter.process;

public interface SourceToTargetProcess<T1, T2> {

  void init();

  void close();

  void process(T1 source, T2 target);
}
