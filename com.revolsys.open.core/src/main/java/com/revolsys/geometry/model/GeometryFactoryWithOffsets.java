package com.revolsys.geometry.model;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.CoordinateSystem;

public class GeometryFactoryWithOffsets extends GeometryFactory {

  private final double offsetX;

  private final double offsetY;

  private final double offsetZ;

  protected GeometryFactoryWithOffsets(final CoordinateSystem coordinateSystem,
    final double offsetX, final double scaleX, final double offsetY, final double scaleY,
    final double offsetZ, final double scaleZ) {
    super(coordinateSystem, 3, scaleX, scaleY, scaleZ);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
  }

  private GeometryFactoryWithOffsets(final CoordinateSystem coordinateSystem, final double offsetX,
    final double offsetY, final double offsetZ, final double[] scales) {
    super(coordinateSystem, 3, scales);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
  }

  public GeometryFactoryWithOffsets(final int coordinateSystemId, final double offsetX,
    final double scaleX, final double offsetY, final double scaleY, final double offsetZ,
    final double scaleZ) {
    super(coordinateSystemId, 3, scaleX, scaleY, scaleZ);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
  }

  public GeometryFactoryWithOffsets(final int coordinateSystemId, final int axisCount,
    final double offset, final double scale) {
    super(coordinateSystemId, axisCount, newScalesFixed(axisCount, scale));
    this.offsetX = offset;
    this.offsetY = offset;
    this.offsetZ = offset;
  }

  @Override
  public GeometryFactoryWithOffsets convertCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    if (isSameCoordinateSystem(coordinateSystem)) {
      return this;
    } else {
      return new GeometryFactoryWithOffsets(coordinateSystem, this.offsetX, this.offsetY,
        this.offsetZ, this.scales);
    }
  }

  @Override
  public double getOffset(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return this.offsetX;
      case 1:
        return this.offsetY;
      case 2:
        return this.offsetZ;
      default:
        return 0;
    }
  }

  @Override
  public double getOffsetX() {
    return this.offsetX;
  }

  @Override
  public double getOffsetY() {
    return this.offsetY;
  }

  @Override
  public double getOffsetZ() {
    return this.offsetZ;
  }

  @Override
  public double toDoubleX(final int x) {
    return this.offsetX + x / this.scaleX;
  }

  @Override
  public double toDoubleY(final int y) {
    return this.offsetY + y / this.scaleY;
  }

  @Override
  public double toDoubleZ(final int z) {
    return this.offsetZ + z / this.scaleZ;
  }

  @Override
  public int toIntX(final double x) {
    return (int)Math.round((x - this.offsetX) / this.resolutionX);
  }

  @Override
  public int toIntY(final double y) {
    return (int)Math.round((y - this.offsetY) / this.resolutionY);
  }

  @Override
  public int toIntZ(final double z) {
    return (int)Math.round((z - this.offsetZ) / this.resolutionZ);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "offsetX", this.offsetX, 0.0);
    addToMap(map, "offsetY", this.offsetY, 0.0);
    addToMap(map, "offsetZ", this.offsetZ, 0.0);
    return map;
  }
}
