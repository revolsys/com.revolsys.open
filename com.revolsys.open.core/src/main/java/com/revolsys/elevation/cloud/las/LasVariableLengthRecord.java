package com.revolsys.elevation.cloud.las;

import java.util.Arrays;

import java.util.function.BiFunction;

public class LasVariableLengthRecord {
  private final String userId;

  private final int recordId;

  private final String description;

  private Object value;

  private final byte[] bytes;

  public LasVariableLengthRecord(final String userId, final int recordId, final String description,
    final byte[] bytes) {
    this.userId = userId;
    this.recordId = recordId;
    this.description = description;
    this.bytes = bytes;
  }

  void convertValue(final BiFunction<LasPointCloud, byte[], Object> converter,
    final LasPointCloud lasPointCloud) {
    this.value = converter.apply(lasPointCloud, this.bytes);
  }

  public byte[] getBytes() {
    return this.bytes;
  }

  public String getDescription() {
    return this.description;
  }

  public int getRecordId() {
    return this.recordId;
  }

  public String getUserId() {
    return this.userId;
  }

  public Object getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    Object value = this.value;
    if (value == null) {
      value = Arrays.toString(this.bytes);
    }
    return this.userId + "-" + this.recordId + "=" + value + " (" + this.description + ")";
  }
}
