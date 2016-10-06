package com.revolsys.elevation.tin;

import java.util.Map;

import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface TriangulatedIrregularNetworkReadFactory extends ReadIoFactory {
  TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(Resource resource,
    Map<String, ? extends Object> properties);
}
