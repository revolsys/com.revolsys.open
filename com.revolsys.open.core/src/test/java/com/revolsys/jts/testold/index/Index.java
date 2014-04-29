package com.revolsys.jts.testold.index;

import java.util.List;

import com.revolsys.jts.geom.Envelope;

/**
 * Adapter for different kinds of indexes
 * @version 1.7
 */
public interface Index {
  void finishInserting();

  void insert(Envelope itemEnv, Object item);

  List query(Envelope searchEnv);
}
