package com.revolsys.jtstest.testbuilder.model;

import com.revolsys.jts.geom.Geometry;

public class ResultGeometryContainer
implements GeometryContainer
{
  private GeometryEditModel geomModel;
  
  public ResultGeometryContainer(GeometryEditModel geomModel) {
    this.geomModel = geomModel;
   }

  public Geometry getGeometry()
  {
    return geomModel.getResult();
  }
}
