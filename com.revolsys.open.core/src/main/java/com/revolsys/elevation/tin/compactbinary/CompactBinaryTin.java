package com.revolsys.elevation.tin.compactbinary;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReadFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriterFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class CompactBinaryTin extends AbstractIoFactoryWithCoordinateSystem
  implements TriangulatedIrregularNetworkReadFactory, TriangulatedIrregularNetworkWriterFactory {

  public static final int HEADER_SIZE = 64;

  public static final short VERSION = 1;

  public static final String FILE_TYPE = "TIN-CB";

  public static final byte[] FILE_TYPE_BYTES = FILE_TYPE.getBytes(StandardCharsets.UTF_8);

  public CompactBinaryTin() {
    super("Compact Binary TIN");
    addMediaTypeAndFileExtension("image/x-rs-compact-binary-tin", "tincb");
  }

  @Override
  public void forEachTriangle(final Resource resource,
    final Map<String, ? extends Object> properties, final TriangleConsumer action) {
    try (
      CompactBinaryTinReader compactBinaryTinReader = new CompactBinaryTinReader(resource)) {
      compactBinaryTinReader.forEachTriangle(action);
    }
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      CompactBinaryTinReader compactBinaryTinReader = new CompactBinaryTinReader(resource)) {
      return compactBinaryTinReader.newTriangulatedIrregularNetwork();
    }
  }

  @Override
  public TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Resource resource) {
    return new CompactBinaryTinWriter(resource);
  }
}
