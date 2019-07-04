package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.awt.Color;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.gradient.GradientStop;
import com.revolsys.elevation.gridded.rasterizer.gradient.MultiStopLinearGradient;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClUtil;

public class JoclSlopeColorGradientRasterizer extends JoclGriddedElevationModelImageRasterizer {
  private static final String SOURCE = OpenClUtil.sourceFromClasspath(
    "com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/SlopeColorGradientRasterizer.cl");

  private final int[] r;

  private final int[] g;

  private final int[] b;

  private final MultiStopLinearGradient gradient;

  public JoclSlopeColorGradientRasterizer(final OpenClDevice device,
    final MultiStopLinearGradient gradient) {
    super(device, SOURCE, "slopeColorGradientRasterizer");
    this.gradient = gradient;
    final List<GradientStop> stops = gradient.getStops();
    this.r = new int[stops.size()];
    this.g = new int[stops.size()];
    this.b = new int[stops.size()];
    int i = 0;
    for (final GradientStop stop : stops) {
      final Color color = stop.getColor();
      this.r[i] = color.getRed();
      this.g[i] = color.getGreen();
      this.b[i] = color.getBlue();
      i++;
    }
  }

  @Override
  protected void addArgs(final List<OpenClMemory> memories,
    final GriddedElevationModel elevationModel, final GeometryFactory geometryFactory,
    final OpenClKernel kernel, final DataType modelDataType) {
    final float offsetZ = (float)geometryFactory.getOffsetZ();
    final float scaleZ = (float)geometryFactory.getScaleZ();

    final MultiStopLinearGradient gradient = this.gradient;
    final List<GradientStop> stops = gradient.getStops();
    final int rangeCount = stops.size();
    final float[] slopes = new float[rangeCount];
    int i = 0;
    for (final GradientStop stop : stops) {
      final double slope = stop.getValue();
      slopes[i] = (float)Math.toRadians(slope);
      i++;
    }
    if (modelDataType == DataTypes.INT) {
      kernel.addArgFloat(offsetZ);
      kernel.addArgFloat(scaleZ);
    }

    kernel.addArgFloat(1.0 / (8 * elevationModel.getGridCellWidth()));
    kernel.addArgFloat(1.0 / (8 * elevationModel.getGridCellHeight()));
    kernel.addArgInt(rangeCount);

    kernel.addArgMemory(memories, slopes);
    kernel.addArgMemory(memories, this.r);
    kernel.addArgMemory(memories, this.g);
    kernel.addArgMemory(memories, this.b);
  }
}
