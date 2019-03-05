package com.revolsys.elevation.cloud.las.zip;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.elevation.cloud.las.LasVariableLengthRecord;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingEncoder;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;

public class LasZipPointCloudWriter extends LasPointCloudWriter {
  private LasZipCompressorType compressor = LasZipCompressorType.POINTWISE;

  private LasZipHeader lasZipHeader;

  private boolean isNewHeader = false;

  private int lasZipVersion = 1;

  public LasZipPointCloudWriter(final Resource resource) {
    super(resource);
  }

  public LasZipCompressorType getCompressor() {
    return this.compressor;
  }

  @Override
  protected Map<Pair<String, Integer>, LasVariableLengthRecord> getLasProperties(
    final LasPointCloudHeader header) {
    final LasPointCloud pointCloud = header.getPointCloud();
    Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = super.getLasProperties(
      header);
    if (this.isNewHeader) {
      lasProperties = new LinkedHashMap<>(lasProperties);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(pointCloud,
        LasZipHeader.KAY_LAS_ZIP, "laszip", this.lasZipHeader);
      lasProperties.put(LasZipHeader.KAY_LAS_ZIP, property);
    }
    return lasProperties;
  }

  public int getLasZipVersion() {
    return this.lasZipVersion;
  }

  public void setCompressor(final LasZipCompressorType compressor) {
    this.compressor = compressor;
  }

  public void setLasZipVersion(final int lasZipVersion) {
    this.lasZipVersion = lasZipVersion;
  }

  @Override
  protected void setPointCloud(final LasPointCloud pointCloud) {
    super.setPointCloud(pointCloud);
    this.lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    if (this.lasZipHeader == null) {
      this.lasZipHeader = LasZipHeader.newLasZipHeader(pointCloud, this.compressor,
        this.lasZipVersion);
      this.isNewHeader = true;
    }
  }

  @Override
  protected void writePoints(final ChannelWriter out, final List<LasPoint> points) {
    try (
      ArithmeticCodingEncoder encoder = new ArithmeticCodingEncoder(out)) {
      final LasZipItemCodec[] codecs = this.lasZipHeader.newLazCodecs(encoder);
      final Iterator<LasPoint> iterator = points.iterator();
      if (iterator.hasNext()) {
        final LasPoint point = iterator.next();
        point.writeLasPoint(out);
        for (final LasZipItemCodec codec : codecs) {
          codec.init(point);
        }
      }
      while (iterator.hasNext()) {
        final LasPoint point = iterator.next();
        for (final LasZipItemCodec codec : codecs) {
          codec.write(point);
        }
      }
    }
  }
}
