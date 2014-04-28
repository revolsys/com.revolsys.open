package com.revolsys.jtstest.testbuilder.model;

import com.revolsys.jts.geom.Geometry;

public class IndexedGeometryContainer
implements GeometryContainer
{
  private GeometryEditModel geomModel;
  private int index;
  
  public IndexedGeometryContainer(GeometryEditModel geomModel, int index) {
    this.geomModel = geomModel;
    this.index = index;
  }

  public Geometry getGeometry()
  {
    return geomModel.getGeometry(index);
  }

}
