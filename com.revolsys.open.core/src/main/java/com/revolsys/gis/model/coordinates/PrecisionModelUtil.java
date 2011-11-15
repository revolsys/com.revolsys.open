package com.revolsys.gis.model.coordinates;

import com.vividsolutions.jts.geom.PrecisionModel;

public class PrecisionModelUtil {
  public static PrecisionModel getPrecisionModel(
    final CoordinatesPrecisionModel coordinatesPrecisionModel) {
    if (coordinatesPrecisionModel instanceof PrecisionModel) {
      return (PrecisionModel)coordinatesPrecisionModel;
    } else if (coordinatesPrecisionModel instanceof SimpleCoordinatesPrecisionModel) {
      SimpleCoordinatesPrecisionModel simpleModel = (SimpleCoordinatesPrecisionModel)coordinatesPrecisionModel;
      final double scaleXY = simpleModel.getScaleXY();
      if (scaleXY <= 0) {
        return new PrecisionModel();
      } else {
        return new PrecisionModel(scaleXY);
      }
    } else {
      return new PrecisionModel();
    }
  }
}
