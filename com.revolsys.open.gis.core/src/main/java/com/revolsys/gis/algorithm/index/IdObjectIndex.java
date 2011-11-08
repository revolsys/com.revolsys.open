package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public interface IdObjectIndex<T> extends Iterable<T> {

  Envelope getEnvelope(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

}
