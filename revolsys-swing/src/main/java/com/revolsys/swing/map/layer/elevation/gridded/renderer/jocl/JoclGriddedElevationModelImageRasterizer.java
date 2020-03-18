package com.revolsys.swing.map.layer.elevation.gridded.renderer.jocl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;

import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.GriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.SlopeColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.gradient.MultiStopLinearGradient;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.jocl.core.OpenClCommandQueue;
import com.revolsys.jocl.core.OpenClContextForDevice;
import com.revolsys.jocl.core.OpenClDevice;
import com.revolsys.jocl.core.OpenClKernel;
import com.revolsys.jocl.core.OpenClMemory;
import com.revolsys.jocl.core.OpenClPlatform;
import com.revolsys.jocl.core.OpenClProgram;
import com.revolsys.jocl.core.OpenClUtil;

public abstract class JoclGriddedElevationModelImageRasterizer
  implements GriddedElevationModelImageRasterizer, BaseCloseable {
  private static final String COMMON_SOURCE = OpenClUtil
    .sourceFromClasspath("com/revolsys/swing/map/layer/elevation/gridded/renderer/jocl/Common.cl");

  public static GriddedElevationModelImageRasterizer newJoclRasterizer(
    final GriddedElevationModelRasterizer javaRasterizer) {
    final List<OpenClPlatform> platforms = OpenClUtil.getPlatforms();
    if (!platforms.isEmpty()) {
      final OpenClPlatform platform = platforms.get(0);
      final List<OpenClDevice> devices = platform.getDevices(CL.CL_DEVICE_TYPE_GPU);
      if (!devices.isEmpty()) {
        final OpenClDevice device = devices.get(devices.size() - 1);
        return newJoclRasterizer(device, javaRasterizer);
      }
    }
    return null;
  }

  public static GriddedElevationModelImageRasterizer newJoclRasterizer(final OpenClDevice device,
    final GriddedElevationModelRasterizer javaRasterizer) {
    if (javaRasterizer instanceof HillShadeGriddedElevationModelRasterizer) {
      final HillShadeGriddedElevationModelRasterizer rasterizer = (HillShadeGriddedElevationModelRasterizer)javaRasterizer;
      return new JoclHillshadeRasterizer(device, rasterizer);
    } else if (javaRasterizer instanceof ColorGriddedElevationModelRasterizer) {
      final ColorGriddedElevationModelRasterizer rasterizer = (ColorGriddedElevationModelRasterizer)javaRasterizer;
      return new JoclColorRasterizer(device, rasterizer);
    } else if (javaRasterizer instanceof SlopeColorGradientGriddedElevationModelRasterizer) {
      final SlopeColorGradientGriddedElevationModelRasterizer rasterizer = (SlopeColorGradientGriddedElevationModelRasterizer)javaRasterizer;
      final MultiStopLinearGradient gradient = (MultiStopLinearGradient)rasterizer.getGradient();
      return new JoclSlopeColorGradientRasterizer(device, gradient);
    } else if (javaRasterizer instanceof ColorGradientGriddedElevationModelRasterizer) {
      final ColorGradientGriddedElevationModelRasterizer rasterizer = (ColorGradientGriddedElevationModelRasterizer)javaRasterizer;
      return new JoclColorGradientRasterizer(device, rasterizer);
    } else {
      return null;
    }
  }

  private final OpenClContextForDevice context;

  private final OpenClCommandQueue commandQueue;

  private final OpenClProgram program;

  private final String kernelName;

  public JoclGriddedElevationModelImageRasterizer(final OpenClDevice device, final String source,
    final String kernelName) {
    this.context = device.newContext();
    this.commandQueue = this.context.newCommandQueue();
    this.program = this.context.newProgram(COMMON_SOURCE, source);
    this.kernelName = kernelName;
  }

  protected abstract void addArgs(List<OpenClMemory> memories, GriddedElevationModel elevationModel,
    GeometryFactory geometryFactory, final OpenClKernel kernel, DataType modelDataType);

  @Override
  public void close() {
    this.program.close();
    this.commandQueue.close();
    this.context.close();
  }

  @Override
  public void rasterize(final GriddedElevationModel elevationModel, final BufferedImage image) {
    if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
      throw new IllegalArgumentException("Destination image is not TYPE_INT_ARGB");
    }
    final int gridWidth = elevationModel.getGridWidth();
    final int gridHeight = elevationModel.getGridHeight();
    if (gridWidth != image.getWidth() || gridHeight != image.getHeight()) {
      throw new IllegalArgumentException("Images do not have the same size");
    }

    DataType modelDataType = DataTypes.DOUBLE;
    if (elevationModel instanceof IntArrayScaleGriddedElevationModel) {
      modelDataType = DataTypes.INT;
    } else if (elevationModel instanceof FloatArrayGriddedElevationModel) {
      modelDataType = DataTypes.FLOAT;
    }

    final List<OpenClMemory> memories = new ArrayList<>();
    try (
      final OpenClKernel kernel = this.program.newKernel(this.kernelName + "_" + modelDataType)) {

      GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
      final double scaleZ = geometryFactory.getScaleZ();
      if (scaleZ <= 0) {
        final double scaleX = geometryFactory.getScaleX();
        final double scaleY = geometryFactory.getScaleY();
        geometryFactory = geometryFactory.convertScales(scaleX, scaleY, 1000);
      }

      if (modelDataType == DataTypes.INT) {
        final int cells[] = elevationModel.getCellsInt();
        kernel.addArgMemory(memories, cells);
      } else if (modelDataType == DataTypes.FLOAT) {
        final float cells[] = elevationModel.getCellsFloat();
        kernel.addArgMemory(memories, cells);
      } else {
        final double cells[] = elevationModel.getCellsDouble();
        kernel.addArgMemory(memories, cells);
      }
      kernel //
        .addArg(gridWidth) //
        .addArg(gridHeight) //
      ;

      addArgs(memories, elevationModel, geometryFactory, kernel, modelDataType);

      final int outputSize = gridWidth * gridHeight * Sizeof.cl_int;
      final OpenClMemory outputImageMem = kernel.addArgNewMemory(memories, outputSize);

      this.commandQueue.enqueueNDRangeKernel(kernel, gridWidth, gridHeight);

      final DataBufferInt dataBufferDst = (DataBufferInt)image.getRaster().getDataBuffer();
      final int dataDst[] = dataBufferDst.getData();
      this.commandQueue.readBuffer(outputImageMem, outputSize, Pointer.to(dataDst));
    } finally {
      for (final OpenClMemory memory : memories) {
        memory.close();
      }
    }
  }
}
