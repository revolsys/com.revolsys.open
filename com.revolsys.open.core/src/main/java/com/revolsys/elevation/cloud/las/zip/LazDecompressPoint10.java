package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public abstract class LazDecompressPoint10 implements LazDecompress {

  protected int classificationByte;

  protected final ArithmeticDecoder decoder;

  protected final ArithmeticModel[] decompressBitByte = new ArithmeticModel[256];

  protected final ArithmeticModel decompressChangedValues = ArithmeticDecoder.createSymbolModel(64);

  protected final ArithmeticModel[] decompressClassification = new ArithmeticModel[256];

  protected IntegerCompressor decompressDeltaX;

  protected IntegerCompressor decompressDeltaY;

  protected IntegerCompressor decompressIntensity;

  protected IntegerCompressor decompressPointSourceId;

  protected final ArithmeticModel[] decompressUserData = new ArithmeticModel[256];

  protected IntegerCompressor decompressZ;

  protected int intensity = 0;

  private final LasPointCloud pointCloud;

  protected int pointSourceId;

  protected int returnByte;

  protected byte scanAngleRank;

  protected boolean scanDirectionFlag;

  protected int userData;

  protected int x;

  protected int y;

  protected int z;

  public LazDecompressPoint10(final LasPointCloud pointCloud, final ArithmeticDecoder decoder) {
    this.pointCloud = pointCloud;
    this.decoder = decoder;
  }

  @Override
  public void init(final LasPoint point) {
    this.decompressDeltaX.initDecompressor();
    this.decompressDeltaY.initDecompressor();
    this.decompressZ.initDecompressor();
    this.decompressIntensity.initDecompressor();
    this.decompressPointSourceId.initDecompressor();

    ArithmeticModel.initSymbolModel(this.decompressChangedValues);
    ArithmeticModel.initSymbolModels(this.decompressBitByte);
    ArithmeticModel.initSymbolModels(this.decompressClassification);
    ArithmeticModel.initSymbolModels(this.decompressUserData);

    final double x = point.getX();
    this.x = this.pointCloud.toIntX(x);
    final double y = point.getY();
    this.y = this.pointCloud.toIntY(y);
    final double z = point.getZ();
    this.z = this.pointCloud.toIntZ(z);

    this.classificationByte = (short)Byte.toUnsignedInt(point.getClassificationByte());
    this.returnByte = (short)Byte.toUnsignedInt(point.getReturnByte());
    this.userData = point.getUserData();
    this.scanDirectionFlag = point.isScanDirectionFlag();
    this.scanAngleRank = point.getScanAngleRank();
    this.pointSourceId = point.getPointSourceID();
  }

  protected void postRead(final LasPoint point) {
    point.setXYZ(this.x, this.y, this.z);
    point.setIntensity(this.intensity);
    point.setReturnByte((byte)this.returnByte);

    point.setClassificationByte((byte)this.classificationByte);
    point.setScanAngleRank(this.scanAngleRank);
    point.setUserData((short)this.userData);
    point.setPointSourceID(this.pointSourceId);
  }

  protected int read(final ArithmeticModel[] models, final int lastValue) {
    ArithmeticModel model = models[lastValue];
    if (model == null) {
      model = ArithmeticDecoder.createSymbolModel(256);
      models[lastValue] = model;
      ArithmeticModel.initSymbolModel(model);
    }

    final int newValue = this.decoder.decodeSymbol(model);
    return newValue;
  }
}
