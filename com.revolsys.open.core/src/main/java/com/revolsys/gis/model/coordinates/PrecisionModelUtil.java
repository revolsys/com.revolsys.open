package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.PrecisionModel;

public class PrecisionModelUtil {
  public static PrecisionModel getPrecisionModel(
    final CoordinatesPrecisionModel coordinatesPrecisionModel) {
    if (coordinatesPrecisionModel instanceof PrecisionModel) {
      return (PrecisionModel)coordinatesPrecisionModel;
    } else if (coordinatesPrecisionModel instanceof SimpleCoordinatesPrecisionModel) {
      final SimpleCoordinatesPrecisionModel simpleModel = (SimpleCoordinatesPrecisionModel)coordinatesPrecisionModel;
      final double scaleXY = simpleModel.getScaleXY();
      return getPrecisionModel(scaleXY);
    } else {
      return new PrecisionModel();
    }
  }

  public static PrecisionModel getPrecisionModel(final double scaleXY) {
    if (scaleXY <= 0) {
      return new PrecisionModel();
    } else {
      return new PrecisionModel(scaleXY);
    }
  }
}
