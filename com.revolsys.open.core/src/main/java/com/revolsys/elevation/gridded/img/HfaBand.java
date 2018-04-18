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

  private final int blockWidth;

  private final int blockHeight;

  private final int gridWidth;

  private final int gridHeight;

  private final int blockRowCount;

  private final int blockColumnCount;

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
    this.blockWidth = node.getInteger("blockWidth");
    this.blockHeight = node.getInteger("blockHeight");
    this.gridWidth = node.getInteger("width");
    this.gridHeight = node.getInteger("height");
    // ceil division (n + d -1) / d
    this.blockRowCount = (this.gridWidth + this.blockWidth - 1) / this.blockWidth;
    this.blockColumnCount = (this.gridHeight + this.blockHeight - 1) / this.blockHeight;

    this.nBlocks = this.blockColumnCount * this.blockColumnCount;
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
    final int cellCount = this.blockWidth * this.blockHeight;
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
    final int blockIndex = blockY * this.blockRowCount + blockX;
    return getBlock(blockIndex);
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
                  block[i] = reader.readFloat();
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
        double gridCellWidth = reader.getGridCellWidth();
        double gridCellHeight = reader.getGridCellHeight();
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

  void uncompressBlock(final float[] block) {
    final int nMaxPixels = block.length;

    final int minValue = this.reader.readInt();
    final int nNumRuns = this.reader.readInt();
    final int nDataOffset = this.reader.readInt();
    final int nNumBits = this.reader.readByte();

    // If this is not run length encoded, but just reduced
    // precision, handle it now.

    int nPixelsOutput = 0;

    if (nNumRuns == -1) {
      // pabyValues = pabyCData + 13;
      // nValueBitOffset = 0;

      if (nNumBits > Integer.MAX_VALUE / nMaxPixels || nNumBits * nMaxPixels > Integer.MAX_VALUE - 7
        || (nNumBits * nMaxPixels + 7) / 8 > Integer.MAX_VALUE - 13) {
        throw new RuntimeException("Integer overflow : nNumBits * nMaxPixels + 7");
      }

      // Loop over block pixels.
      for (nPixelsOutput = 0; nPixelsOutput < nMaxPixels; nPixelsOutput++) {
        // Extract the data value in a way that depends on the number
        // of bits in it.

        int rawValue = 0;

        if (nNumBits == 0) {
          // nRawValue = 0;
        } else if (nNumBits == 1) {
          // nRawValue = pabyValues[nValueBitOffset >> 3] >> (nValueBitOffset & 7) & 0x1;
          // nValueBitOffset++;
          throw new RuntimeException();
        } else if (nNumBits == 2) {
          // nRawValue = pabyValues[nValueBitOffset >> 3] >> (nValueBitOffset & 7) & 0x3;
          // nValueBitOffset += 2;
          throw new RuntimeException();
        } else if (nNumBits == 4) {
          // nRawValue = pabyValues[nValueBitOffset >> 3] >> (nValueBitOffset & 7) & 0xf;
          // nValueBitOffset += 4;
          throw new RuntimeException();
        } else if (nNumBits == 8) {
          rawValue = this.reader.readByte();
        } else if (nNumBits == 16) {
          rawValue = this.reader.readShort();
        } else if (nNumBits == 32) {
          rawValue = this.reader.readInt();
        } else {
          throw new RuntimeException("Unsupported nNumBits value: " + nNumBits);
        }

        // Offset by the minimum value.
        final int intValue = minValue + rawValue;

        block[nPixelsOutput] = Float.intBitsToFloat(intValue);

      }

    }

    // Establish data pointers for runs.
    if (nNumRuns < 0 || nDataOffset < 0) {
      throw new RuntimeException(
        String.format("nNumRuns=%d, nDataOffset=%d", nNumRuns, nDataOffset));
    }

    if (nNumRuns != 0
      && (nNumBits > Integer.MAX_VALUE / nNumRuns || nNumBits * nNumRuns > Integer.MAX_VALUE - 7
        || (nNumBits * nNumRuns + 7) / 8 > Integer.MAX_VALUE - nDataOffset)) {
      throw new RuntimeException("Integer overflow: nDataOffset + (nNumBits * nNumRuns + 7)/8");
    }

    // nValueBitOffset = 0;

    // Loop over runs.
    for (int iRun = 0; iRun < nNumRuns; iRun++) {
      int nRepeatCount = 0;
      final short firstByte = this.reader.readUnsignedByte();
      // Get the repeat count. This can be stored as one, two, three
      // or four bytes depending on the low order two bits of the
      // first byte.
      if ((firstByte & 0xc0) == 0x00) {
        nRepeatCount = firstByte & 0x3f;
      } else if ((firstByte & 0xc0) == 0x40) {
        nRepeatCount = firstByte & 0x3f;
        nRepeatCount = nRepeatCount * 256 + this.reader.readUnsignedByte();
      } else if ((firstByte & 0xc0) == 0x80) {
        nRepeatCount = firstByte & 0x3f;
        nRepeatCount = nRepeatCount * 256 + this.reader.readUnsignedByte();
        nRepeatCount = nRepeatCount * 256 + this.reader.readUnsignedByte();
      } else if ((firstByte & 0xc0) == 0xc0) {
        nRepeatCount = firstByte & 0x3f;
        nRepeatCount = nRepeatCount * 256 + this.reader.readUnsignedByte();
        nRepeatCount = nRepeatCount * 256 + this.reader.readUnsignedByte();
        nRepeatCount = nRepeatCount * 256 + this.reader.readUnsignedByte();
      }

      // Extract the data value in a way that depends on the number
      // of bits in it.
      int nDataValue = 0;

      if (nNumBits == 0) {
        // nDataValue = 0;
      } else if (nNumBits == 1) {
        // nDataValue = pabyValues[nValueBitOffset >> 3] >> (nValueBitOffset & 7) & 0x1;
        // nValueBitOffset++;
        throw new RuntimeException();
      } else if (nNumBits == 2) {
        // nDataValue = pabyValues[nValueBitOffset >> 3] >> (nValueBitOffset & 7) & 0x3;
        // nValueBitOffset += 2;
        throw new RuntimeException();
      } else if (nNumBits == 4) {
        // nDataValue = pabyValues[nValueBitOffset >> 3] >> (nValueBitOffset & 7) & 0xf;
        // nValueBitOffset += 4;
        throw new RuntimeException();
      } else if (nNumBits == 8) {
        nDataValue = this.reader.readUnsignedByte();
      } else if (nNumBits == 16) {
        nDataValue = 256 * this.reader.readUnsignedByte();
        nDataValue += this.reader.readUnsignedByte();
      } else if (nNumBits == 32) {
        nDataValue = this.reader.readUnsignedByte() & 0x3f;
        nDataValue = nDataValue * 256 + this.reader.readUnsignedByte();
        nDataValue = nDataValue * 256 + this.reader.readUnsignedByte();
        nDataValue = nDataValue * 256 + this.reader.readUnsignedByte();

      } else {
        throw new RuntimeException("nNumBits = " + nNumBits);
      }

      // Offset by the minimum value.
      nDataValue += minValue;

      // Now apply to the output buffer in a type specific way.
      if (nRepeatCount > Integer.MAX_VALUE - nPixelsOutput
        || nPixelsOutput + nRepeatCount > nMaxPixels) {
        nRepeatCount = nMaxPixels - nPixelsOutput;
      }

      final float fDataValue = Float.intBitsToFloat(nDataValue);

      for (int i = 0; i < nRepeatCount; i++) {
        block[nPixelsOutput++] = fDataValue;
      }
    }
  }
}
