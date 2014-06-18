package com.revolsys.jts.testold.index;

import java.util.List;

import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

/**
 * Adapter for different kinds of indexes
 * @version 1.7
 */
public interface Index {
  void finishInserting();

  void insert(BoundingBoxDoubleGf itemEnv, Object item);

  List query(BoundingBoxDoubleGf searchEnv);
}
