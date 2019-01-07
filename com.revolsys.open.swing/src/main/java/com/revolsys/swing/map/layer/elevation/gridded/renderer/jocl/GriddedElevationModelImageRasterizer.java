package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.awt.image.BufferedImage;

import com.revolsys.elevation.gridded.GriddedElevationModel;

public interface GriddedElevationModelImageRasterizer {

  void rasterize(final GriddedElevationModel elevationModel, final BufferedImage image);
}
