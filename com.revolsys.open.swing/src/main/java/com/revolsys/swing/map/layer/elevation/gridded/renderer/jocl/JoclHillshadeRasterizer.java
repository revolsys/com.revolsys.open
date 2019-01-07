package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.util.List;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClUtil;

public class JoclHillshadeRasterizer extends JoclGriddedElevationModelImageRasterizer {
  private static final String SOURCE = OpenClUtil.sourceFromClasspath(
    "com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/HillshadeRasterizerInt.cl");

  private final HillShadeGriddedElevationModelRasterizer rasterizer;

  public JoclHillshadeRasterizer(final OpenClDevice device,
    final HillShadeGriddedElevationModelRasterizer rasterizer) {
    super(device, SOURCE, "hillshadeRasterizerInt");
    this.rasterizer = rasterizer;
  }

  @Override
  protected void addArgs(final List<OpenClMemory> memories,
    final GriddedElevationModel elevationModel, final GeometryFactory geometryFactory,
    final OpenClKernel kernel) {
    final double offsetZ = geometryFactory.getOffsetZ();
    final double scaleZ = geometryFactory.getScaleZ();
    kernel//
      .addArg(offsetZ) //
      .addArg(scaleZ) //
      .addArg(this.rasterizer.getAzimuthRadians()) //
      .addArg(this.rasterizer.getCosZenithRadians()) //
      .addArg(this.rasterizer.getSinZenithRadians()) //
      .addArg(this.rasterizer.getZFactor()) //
      .addArg(this.rasterizer.getOneDivCellSizeTimes8());
  }
}
