package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClUtil;

public class JoclHillshadeRasterizer extends JoclGriddedElevationModelImageRasterizer {
  private static final String SOURCE = OpenClUtil.sourceFromClasspath(
    "com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/HillshadeRasterizer.cl");

  private final HillShadeGriddedElevationModelRasterizer rasterizer;

  public JoclHillshadeRasterizer(final OpenClDevice device,
    final HillShadeGriddedElevationModelRasterizer rasterizer) {
    super(device, SOURCE, "hillshadeRasterizer");
    this.rasterizer = rasterizer;
  }

  @Override
  protected void addArgs(final List<OpenClMemory> memories,
    final GriddedElevationModel elevationModel, final GeometryFactory geometryFactory,
    final OpenClKernel kernel, final DataType modelDataType) {
    final double offsetZ = geometryFactory.getOffsetZ();
    final double scaleZ = geometryFactory.getScaleZ();

    final double gridCellWidth = elevationModel.getGridCellWidth();
    final double gridCellHeight = elevationModel.getGridCellHeight();
    final double xFactor = 1.0 / (8 * gridCellWidth);
    final double yFactor = 1.0 / (8 * gridCellHeight);

    if (modelDataType == DataTypes.INT) {
      kernel//
        .addArgFloat(offsetZ) //
        .addArgFloat(scaleZ) //
      ;
    }
    kernel //
      .addArgFloat(this.rasterizer.getAzimuthRadians()) //
      .addArgFloat(this.rasterizer.getCosZenithRadians()) //
      .addArgFloat(this.rasterizer.getSinZenithRadians()) //
      .addArgFloat(xFactor) //
      .addArgFloat(yFactor) //
      .addArgFloat(this.rasterizer.getZFactor()) //
    ;
  }
}
