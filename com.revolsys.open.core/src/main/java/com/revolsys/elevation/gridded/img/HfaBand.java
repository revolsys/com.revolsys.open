package com.revolsys.elevation.gridded.img;

import java.util.Arrays;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.FloatArrayGrid;

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

  HfaEntry node;

  private final int blockWidth;

  private final int blockHeight;

  private final int gridWidth;

  private final int gridHeight;

  private final int blockRowCount;

  private final int blockColumnCount;

  private boolean bNoDataSet;

  private double nullValue;

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
    this.node = node;
    this.dataType = node.getString("pixelType");
    this.blockWidth = node.getInteger("blockWidth");
    this.blockHeight = node.getInteger("blockHeight");
    this.gridWidth = node.getInteger("width");
    this.gridHeight = node.getInteger("height");
    // ceil division (n + d -1) / d
    this.blockRowCount = (this.gridWidth + this.blockWidth - 1) / this.blockWidth;
    this.blockColumnCount = (this.gridHeight + this.blockHeight - 1) / this.blockHeight;

    this.nBlocks = this.blockColumnCount * this.blockColumnCount;

    final HfaEntry noDataNode = this.node.GetNamedChild("Eimg_NonInitializedValue");
    if (noDataNode != null) {
      final Object nullValue = noDataNode.getValue("valueBD");
      if (nullValue instanceof Double) {
        this.nullValue = (Double)nullValue;
      } else if (nullValue instanceof HfaBinaryData) {
        final HfaBinaryData nullData = (HfaBinaryData)nullValue;
        this.nullValue = ((double[])nullData.getData())[0];
      }
    }
  }

  public String getBandName() {
    final String bandName = this.node.getName();
    if (bandName.length() > 0) {
      return bandName;
    } else {
      return "Layer_" + this.bandNumber;
    }
  }

  public GriddedElevationModel getGriddedElevationModel() {
    if (this.elevationModel == null) {
      loadBlockInfo();
      final int gridWidth = this.gridWidth;
      final int gridHeight = this.gridHeight;
      final int cellCount = gridWidth * gridHeight;
      final int blockColumnCount = this.blockColumnCount;
      final int blockRowCount = this.blockRowCount;
      final int blockWidth = this.blockWidth;
      final int blockHeight = this.blockHeight;
      final ImgGriddedElevationReader reader = this.reader;
      final List<MapEx> blockInfoList = this.blockInfoList;
      if ("f32".equals(this.dataType)) {
        final int blockCellCount = blockWidth * blockHeight;
        final float[] block = new float[blockCellCount];
        final float[] cells = new float[cellCount];
        Arrays.fill(cells, FloatArrayGrid.NULL_VALUE);
        int blockIndex = 0;
        for (int blockY = 0; blockY < blockRowCount; blockY++) {

          final int blockGridCellMaxY = gridHeight - 1 - blockY * blockWidth;
          int blockGridCellMinY = blockGridCellMaxY - blockHeight;
          if (blockGridCellMinY < 0) {
            blockGridCellMinY = 0;
          }

          for (int blockX = 0; blockX < blockColumnCount; blockX++) {

            final int blockGridCellMinX = blockX * blockWidth;
            int blockGridCellMaxX = blockGridCellMinX + blockWidth;
            int xCount;
            if (blockGridCellMaxX > gridWidth) {
              blockGridCellMaxX = gridWidth;
              xCount = gridWidth - blockGridCellMinX;
            } else {
              xCount = blockWidth;
            }

            final MapEx blockInfo = blockInfoList.get(blockIndex);
            if (blockInfo.getBoolean("logvalid")) {
              final String compressionType = blockInfo.getString("compressionType");
              final long offset = blockInfo.getLong("offset");
              reader.seek(offset);

              if ("no compression".equals(compressionType)) {
                for (int i = 0; i < blockCellCount; i++) {
                  final float value = reader.readFloat();
                  if (value == this.nullValue) {
                    block[i] = FloatArrayGrid.NULL_VALUE;
                  } else {
                    block[i] = value;
                  }
                }

              } else if ("ESRI GRID compression".equals(compressionType)) {
                uncompressBlock(block);
              } else {
                throw new RuntimeException(compressionType + " not supported");
              }
              int blockOffset = 0;
              for (int gridY = blockGridCellMaxY; gridY > blockGridCellMinY; gridY--) {
                final int rowOffset = gridY * gridWidth + blockX * blockWidth;
                System.arraycopy(block, blockOffset, cells, rowOffset, xCount);
                blockOffset += blockWidth;
              }

            }

            blockIndex++;
          }
        }
        final GeometryFactory geometryFactory = reader.getGeometryFactory();
        final BoundingBox boundingBox = reader.getBoundingBox();
        final double gridCellWidth = reader.getGridCellWidth();
        final double gridCellHeight = reader.getGridCellHeight();
        this.elevationModel = new FloatArrayGriddedElevationModel(geometryFactory, boundingBox,
          gridWidth, gridHeight, gridCellWidth, gridCellHeight, cells);
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
      final HfaEntry rasterDMSEntry = this.node.GetNamedChild("RasterDMS");
      if (rasterDMSEntry == null) {
        if (this.node.GetNamedChild("ExternalRasterDMS") != null) {
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

  void uncompressBlock(final float[] block) {
    final int blockLength = block.length;

    final int minValue = this.reader.readInt();
    final float minFloat = Float.intBitsToFloat(minValue);
    final int runCount = this.reader.readInt();
    final int offset = this.reader.readInt();
    final int bitCount = this.reader.readByte();

    // If this is not run length encoded, but just reduced
    // precision, handle it now.

    int cellIndex = 0;

    if (runCount == -1) {
      int valueBitOffset = 0;

      if (bitCount > Integer.MAX_VALUE / blockLength
        || bitCount * blockLength > Integer.MAX_VALUE - 7
        || (bitCount * blockLength + 7) / 8 > Integer.MAX_VALUE - 13) {
        throw new RuntimeException("Integer overflow : nNumBits * nMaxPixels + 7");
      }

      // Loop over block pixels.
      for (cellIndex = 0; cellIndex < blockLength; cellIndex++) {
        // Extract the data value in a way that depends on the number
        // of bits in it.

        int rawValue = 0;
        byte bitFlags = 0;

        if (bitCount == 0) {
          // rawValue = 0;
        } else if (bitCount == 1) {
          if (valueBitOffset % 8 == 0) {
            bitFlags = this.reader.readByte();
            valueBitOffset = 0;
          }
          rawValue = bitFlags >> valueBitOffset & 0x1;
          valueBitOffset++;
        } else if (bitCount == 2) {
          if (valueBitOffset % 8 == 0) {
            bitFlags = this.reader.readByte();
            valueBitOffset = 0;
          }
          rawValue = bitFlags >> valueBitOffset & 0x3;
          valueBitOffset++;
        } else if (bitCount == 4) {
          rawValue = bitFlags >> valueBitOffset & 0xf;
          valueBitOffset++;
        } else if (bitCount == 8) {
          rawValue = this.reader.readByte();
        } else if (bitCount == 16) {
          rawValue = this.reader.readShort();
        } else if (bitCount == 32) {
          rawValue = this.reader.readInt();
        } else {
          throw new RuntimeException("Unsupported nNumBits value: " + bitCount);
        }

        // Offset by the minimum value.
        final int intValue = minValue + rawValue;

        block[cellIndex] = Float.intBitsToFloat(intValue);

      }

    } else if (runCount < 0 || offset < 0) {
      throw new RuntimeException(String.format("nNumRuns=%d, nDataOffset=%d", runCount, offset));
    } else if (runCount != 0
      && (bitCount > Integer.MAX_VALUE / runCount || bitCount * runCount > Integer.MAX_VALUE - 7
        || (bitCount * runCount + 7) / 8 > Integer.MAX_VALUE - offset)) {
      throw new RuntimeException("Integer overflow: nDataOffset + (nNumBits * nNumRuns + 7)/8");
    } else {

      final int[] reapeatCounts = new int[runCount];
      for (int runIndex = 0; runIndex < runCount; runIndex++) {
        int repeatCount = 0;
        final short firstByte = this.reader.readUnsignedByte();
        // Get the repeat count. This can be stored as one, two, three
        // or four bytes depending on the low order two bits of the
        // first byte.
        if ((firstByte & 0xc0) == 0x00) {
          repeatCount = firstByte & 0x3f;
        } else if ((firstByte & 0xc0) == 0x40) {
          repeatCount = firstByte & 0x3f;
          repeatCount = repeatCount * 256 + this.reader.readUnsignedByte();
        } else if ((firstByte & 0xc0) == 0x80) {
          repeatCount = firstByte & 0x3f;
          repeatCount = repeatCount * 256 + this.reader.readUnsignedByte();
          repeatCount = repeatCount * 256 + this.reader.readUnsignedByte();
        } else if ((firstByte & 0xc0) == 0xc0) {
          repeatCount = firstByte & 0x3f;
          repeatCount = repeatCount * 256 + this.reader.readUnsignedByte();
          repeatCount = repeatCount * 256 + this.reader.readUnsignedByte();
          repeatCount = repeatCount * 256 + this.reader.readUnsignedByte();
        }
        reapeatCounts[runIndex] = repeatCount;
      }
      int valueBitOffset = 0;

      for (int repeatCount : reapeatCounts) {
        // Extract the data value in a way that depends on the number
        // of bits in it.
        int dataValue = 0;
        byte bitFlags = 0;
        if (bitCount == 0) {
          // dataValue = 0;
        } else if (bitCount == 1) {
          if (valueBitOffset % 8 == 0) {
            bitFlags = this.reader.readByte();
            valueBitOffset = 0;
          }
          dataValue = bitFlags >> valueBitOffset & 0x1;
          valueBitOffset++;
        } else if (bitCount == 2) {
          if (valueBitOffset % 8 == 0) {
            bitFlags = this.reader.readByte();
            valueBitOffset = 0;
          }
          dataValue = bitFlags >> valueBitOffset & 0x3;
          valueBitOffset++;
        } else if (bitCount == 4) {
          dataValue = bitFlags >> valueBitOffset & 0xf;
          valueBitOffset++;
        } else if (bitCount == 8) {
          dataValue = this.reader.readUnsignedByte();
        } else if (bitCount == 16) {
          dataValue = 256 * this.reader.readUnsignedByte();
          dataValue += this.reader.readUnsignedByte();
        } else if (bitCount == 32) {
          final short b1 = this.reader.readUnsignedByte();
          final short b2 = this.reader.readUnsignedByte();
          final short b3 = this.reader.readUnsignedByte();
          final short b4 = this.reader.readUnsignedByte();
          dataValue = b1 & 0x3f;
          dataValue = dataValue * 256 + b2;
          dataValue = dataValue * 256 + b3;
          dataValue = dataValue * 256 + b4;

        } else {
          throw new RuntimeException("nNumBits = " + bitCount);
        }

        // Offset by the minimum value.
        dataValue += minValue;

        // Now apply to the output buffer in a type specific way.
        if (repeatCount > Integer.MAX_VALUE - cellIndex || cellIndex + repeatCount > blockLength) {
          repeatCount = blockLength - cellIndex;
        }

        final float floatValue = Float.intBitsToFloat(dataValue);

        Arrays.fill(block, cellIndex, cellIndex += repeatCount, floatValue);
      }
    }
  }
}
