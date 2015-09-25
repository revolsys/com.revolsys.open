package com.revolsys.geometry.test.old.index;

import java.util.List;

import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;

/**
 * Adapter for different kinds of indexes
 * @version 1.7
 */
public interface Index {
  void finishInserting();

  void insert(BoundingBoxDoubleGf itemEnv, Object item);

  List query(BoundingBoxDoubleGf searchEnv);
}
