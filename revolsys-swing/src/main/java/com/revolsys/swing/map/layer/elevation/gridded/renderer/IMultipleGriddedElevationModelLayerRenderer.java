package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;

public interface IMultipleGriddedElevationModelLayerRenderer
  extends MultipleLayerRenderer<ElevationModelLayer, AbstractGriddedElevationModelLayerRenderer> {

  ElevationModelLayer getLayer();

}
