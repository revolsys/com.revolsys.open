package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.LasPoint;
import com.revolsys.elevation.cloud.las.LasPointCloud;

public abstract class LasDecompressPointCore implements LASreadItemCompressed {

  protected final ArithmeticModel changedValues = ArithmeticDecoder.createSymbolModel(64);

  protected short classificationByte;

  protected final ArithmeticDecoder decoder;

  protected final ArithmeticModel[] decompressBitByte = new ArithmeticModel[256];

  protected final ArithmeticModel[] decompressClassification = new ArithmeticModel[256];

  protected IntegerCompressor decompressDeltaX;

  protected IntegerCompressor decompressDeltaY;

  protected IntegerCompressor decompressIntensity;

  protected IntegerCompressor decompressPointSourceId;

  protected final ArithmeticModel[] decompressUserData = new ArithmeticModel[256];

  protected IntegerCompressor decompressZ;

  protected int intensity = 0;

  protected int pointSourceId;

  protected short returnByte;

  protected short scanAngleRank;

  protected boolean scanDirectionFlag;

  protected short userData;

  protected int x;

  protected int y;

  protected int z;

  private final LasPointCloud pointCloud;

  public LasDecompressPointCore(final LasPointCloud pointCloud, final ArithmeticDecoder decoder) {
    this.pointCloud = pointCloud;
    this.decoder = decoder;
  }

  @Override
  public boolean init(final LasPoint point) {
    this.decompressDeltaX.initDecompressor();
    this.decompressDeltaY.initDecompressor();
    this.decompressZ.initDecompressor();
    this.decompressIntensity.initDecompressor();
    this.decompressPointSourceId.initDecompressor();

    ArithmeticModel.initSymbolModel(this.changedValues);
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

    return true;
  }

  protected void postRead(final LasPoint point) {
    final LasPointCloud pointCloud = this.pointCloud;
    point.setX(pointCloud.toDoubleX(this.x));
    point.setY(pointCloud.toDoubleY(this.y));
    point.setZ(pointCloud.toDoubleZ(this.z));
    point.setIntensity(this.intensity);
    point.setReturnByte((byte)this.returnByte);

    point.setClassificationByte((byte)this.classificationByte);
    point.setScanAngleRank(this.scanAngleRank);
    point.setUserData(this.userData);
    point.setPointSourceID(this.pointSourceId);
  }

  protected short read(final ArithmeticModel[] models, final short lastValue) {
    ArithmeticModel model = models[lastValue];
    if (model == null) {
      model = ArithmeticDecoder.createSymbolModel(256);
      models[lastValue] = model;
      ArithmeticModel.initSymbolModel(model);
    }

    final short newValue = (short)this.decoder.decodeSymbol(model);
    return newValue;
  }
}
