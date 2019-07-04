package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClUtil;

public class JoclColorRasterizer extends JoclGriddedElevationModelImageRasterizer {
  private static final String SOURCE = OpenClUtil.sourceFromClasspath(
    "com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/ColorRasterizer.cl");

  private final ColorGriddedElevationModelRasterizer rasterizer;

  public JoclColorRasterizer(final OpenClDevice device,
    final ColorGriddedElevationModelRasterizer rasterizer) {
    super(device, SOURCE, "colorRasterizer");
    this.rasterizer = rasterizer;
  }

  @Override
  protected void addArgs(final List<OpenClMemory> memories,
    final GriddedElevationModel elevationModel, final GeometryFactory geometryFactory,
    final OpenClKernel kernel, final DataType modelDataType) {
    final double minZ = this.rasterizer.getMinZ();
    final double maxZ = this.rasterizer.getMaxZ();
    if (modelDataType == DataTypes.INT) {
      final int minZInt = geometryFactory.toIntZ(minZ);
      final int maxZInt = geometryFactory.toIntZ(maxZ);
      kernel//
        .addArgInt(minZInt) //
        .addArgInt(maxZInt - minZInt) //
      ;
    } else {
      kernel//
        .addArgFloat(minZ) //
        .addArgFloat(maxZ - minZ) //
      ;
    }
    kernel //
      .addArgInt(this.rasterizer.getMinColour().getRGB()) //
      .addArgInt(this.rasterizer.getMaxColour().getRGB()) //
    ;
  }
}
