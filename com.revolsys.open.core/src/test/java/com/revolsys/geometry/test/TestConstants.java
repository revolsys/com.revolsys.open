package com.revolsys.geometry.test;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;

public interface TestConstants {

  GeometryFactory UTM10_GF_2_FLOATING = GeometryFactory.floating2d(EpsgCoordinateSystems.nad83UtmId(10));

  int UTM10_X_START = 500000;

  int UTM10_Y_START = 6000000;
}
