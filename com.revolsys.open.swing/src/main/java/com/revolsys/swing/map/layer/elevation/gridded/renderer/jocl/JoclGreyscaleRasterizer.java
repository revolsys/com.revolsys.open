package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.util.List;

import com.revolsys.datatype.DataType;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClUtil;

public class JoclGreyscaleRasterizer extends JoclGriddedElevationModelImageRasterizer {
  private static final String SOURCE = OpenClUtil.sourceFromClasspath(
    "com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/ColorRasterizerInt.cl");

  private final ColorGriddedElevationModelRasterizer rasterizer;

  public JoclGreyscaleRasterizer(final OpenClDevice device,
    final ColorGriddedElevationModelRasterizer rasterizer) {
    super(device, SOURCE, "colorRasterizer");
    this.rasterizer = rasterizer;
  }

  @Override
  protected void addArgs(final List<OpenClMemory> memories,
    final GriddedElevationModel elevationModel, final GeometryFactory geometryFactory,
    final OpenClKernel kernel, DataType modelDataType) {
    final int minZInt = geometryFactory.toIntZ(this.rasterizer.getMinZ());
    final int maxZInt = geometryFactory.toIntZ(this.rasterizer.getMaxZ());
    kernel//
      .addArgInt(minZInt) //
      .addArgInt(maxZInt - minZInt) //
    ;
  }
}
