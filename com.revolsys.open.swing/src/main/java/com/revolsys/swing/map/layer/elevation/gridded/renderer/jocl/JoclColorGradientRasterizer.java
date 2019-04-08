package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.awt.Color;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.gradient.GradientStop;
import com.revolsys.elevation.gridded.rasterizer.gradient.MultiStopLinearGradient;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClUtil;

public class JoclColorGradientRasterizer extends JoclGriddedElevationModelImageRasterizer {
  private static final String SOURCE = OpenClUtil.sourceFromClasspath(
    "com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/ColorGradientRasterizer.cl");

  private final ColorGradientGriddedElevationModelRasterizer rasterizer;

  private final int[] r;

  private final int[] g;

  private final int[] b;

  public JoclColorGradientRasterizer(final OpenClDevice device,
    final ColorGradientGriddedElevationModelRasterizer rasterizer) {
    super(device, SOURCE, "colorGradientRasterizer");
    this.rasterizer = rasterizer;
    final MultiStopLinearGradient gradient = (MultiStopLinearGradient)rasterizer.getGradient();
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
    final MultiStopLinearGradient gradient = (MultiStopLinearGradient)this.rasterizer.getGradient();
    final List<GradientStop> stops = gradient.getStops();
    final int rangeCount = stops.size();
    kernel.addArgInt(rangeCount);

    if (modelDataType == DataTypes.INT) {
      final int[] z = new int[rangeCount];
      int i = 0;
      for (final GradientStop stop : stops) {
        final double zDouble = stop.getValue();
        z[i++] = geometryFactory.toIntZ(zDouble);
      }
      kernel.addArgMemory(memories, z);
    } else {
      final float[] z = new float[rangeCount];
      int i = 0;
      for (final GradientStop stop : stops) {
        z[i++] = (float)stop.getValue();
      }
      kernel.addArgMemory(memories, z);
    }

    kernel.addArgMemory(memories, this.r);
    kernel.addArgMemory(memories, this.g);
    kernel.addArgMemory(memories, this.b);
  }
}
