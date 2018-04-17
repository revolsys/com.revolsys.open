package com.revolsys.elevation.gridded.img;

import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class HfaBand {

  public static final HfaBand newBand(final ImgGriddedElevationReader reader, final int bandNumber,
    final HfaEntry node) {
    return new HfaBand(reader, bandNumber, node);
  }

  int nBlocks;

  // Used for spill-file modification.
  int nLayerStackCount;

  int nLayerStackIndex;

  int nPCTColors = -1;

  double[] apadfPCT = new double[4];

  double padfPCTBins;

  private final ImgGriddedElevationReader reader;

  private final String dataType;

  HfaEntry poNode;

  private final int nBlockXSize;

  private final int blockHeight;

  private final int gridWidth;

  private final int gridHeight;

  private final int blockCountRow;

  private final int blockCountColumn;

  private boolean bNoDataSet;

  private double dfNoData;

  private final boolean bOverviewsPending = true;

  private int nOverviews;

  private HfaBand papoOverviews;

  private final int bandNumber;

  private List<MapEx> blockInfoList;

  private GriddedElevationModel elevationModel;

  public HfaBand(final ImgGriddedElevationReader reader, final int bandNumber,
    final HfaEntry node) {
    this.reader = reader;
    this.bandNumber = bandNumber;
    this.poNode = node;
    this.dataType = node.getString("pixelType");
    this.nBlockXSize = node.getInteger("blockWidth");
    this.blockHeight = node.getInteger("blockHeight");
    this.gridWidth = node.getInteger("width");
    this.gridHeight = node.getInteger("height");
    this.blockCountRow = this.gridWidth / this.nBlockXSize;
    this.blockCountColumn = this.gridHeight / this.blockHeight;

    this.nBlocks = this.blockCountColumn * this.blockCountColumn;
  }

  public String getBandName() {
    final String bandName = this.poNode.getName();
    if (bandName.length() > 0) {
      return bandName;
    } else {
      return "Layer_" + this.bandNumber;
    }
  }

  public Object getBlock(final int blockIndex) {
    loadBlockInfo();
    final MapEx blockInfo = this.blockInfoList.get(blockIndex);
    final long offset = blockInfo.getLong("offset");
    this.reader.seek(offset);

    final String compressionType = blockInfo.getString("compressionType");

    if (!"no compression".equals(compressionType)) {
      throw new RuntimeException(compressionType + " not supported");
    }
    final int cellCount = this.nBlockXSize * this.blockHeight;
    if ("f32".equals(this.dataType)) {
      final float[] cells = new float[cellCount];
      for (int i = 0; i < cellCount; i++) {
        cells[i] = this.reader.readFloat();
      }
      return cells;
    } else {
      throw new RuntimeException(this.dataType + " not supported");
    }
  }

  public Object getBlock(final int blockX, final int blockY) {
    final int blockIndex = blockY * this.blockCountRow + blockX;
    return getBlock(blockIndex);
  }

  public GriddedElevationModel getGriddedElevationModel() {
    if (this.elevationModel == null) {
      loadBlockInfo();
      final int gridWidth = this.gridWidth;
      final int gridHeight = this.gridHeight;
      final int cellCount = gridWidth * gridHeight;
      final int blockCountColumn = this.blockCountColumn;
      final int blockCountRow = this.blockCountRow;
      final int blockWidth = this.nBlockXSize;
      final int blockHeight = this.blockHeight;
      final ImgGriddedElevationReader reader = this.reader;
      final List<MapEx> blockInfoList = this.blockInfoList;
      if ("f32".equals(this.dataType)) {
        final float[] cells = new float[cellCount];
        for (int blockY = 0; blockY < blockCountColumn; blockY++) {
          int blockIndex = blockY * blockCountRow;
          for (int blockX = 0; blockX < blockCountRow; blockX++) {
            final MapEx blockInfo = blockInfoList.get(blockIndex);
            final String compressionType = blockInfo.getString("compressionType");

            if (!"no compression".equals(compressionType)) {
              throw new RuntimeException(compressionType + " not supported");
            }

            final long offset = blockInfo.getLong("offset");
            reader.seek(offset);

            for (int gridY = 0; gridY < blockHeight; gridY++) {
              int cellIndex = (blockY * blockHeight + gridY) * gridWidth + blockX * blockWidth;
              for (int gridX = 0; gridX < blockWidth; gridX++) {
                cells[cellIndex++] = reader.readFloat();
              }
            }

            blockIndex++;
          }
        }
        final GeometryFactory geometryFactory = reader.getGeometryFactory();
        final BoundingBox boundingBox = reader.getBoundingBox();
        this.elevationModel = new FloatArrayGriddedElevationModel(geometryFactory, boundingBox,
          gridWidth, gridHeight, reader.getPixelSizeHeight(), cells);
      } else {
        throw new RuntimeException(this.dataType + " not supported");
      }
    }
    return this.elevationModel;
  }

  public int getWidth() {
    return this.gridWidth;
  }

  public void loadBlockInfo() {
    if (this.blockInfoList == null) {
      final HfaEntry rasterDMSEntry = this.poNode.GetNamedChild("RasterDMS");
      if (rasterDMSEntry == null) {
        if (this.poNode.GetNamedChild("ExternalRasterDMS") != null) {
          throw new IllegalArgumentException("ExternalRasterDMS is not supporte");
        } else {
          throw new IllegalArgumentException(
            "Can't find RasterDMS field in Eimg_Layer with block list.");
        }
      } else {
        this.blockInfoList = rasterDMSEntry.getValue("blockinfo");
      }
    }
  }

  @Override
  public String toString() {
    return getBandName();
  }
}
