package com.revolsys.core.test.geometry.test.old.index;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;

/**
 * Adapter for different kinds of indexes
 * @version 1.7
 */
public interface Index {
  void finishInserting();

  void insert(BoundingBox itemEnv, Object item);

  List query(BoundingBox searchEnv);
}
