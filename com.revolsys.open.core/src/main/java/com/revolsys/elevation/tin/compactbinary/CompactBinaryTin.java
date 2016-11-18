package com.revolsys.elevation.tin.compactbinary;

import java.util.Map;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReadFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriterFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class CompactBinaryTin extends AbstractIoFactoryWithCoordinateSystem
  implements TriangulatedIrregularNetworkReadFactory, TriangulatedIrregularNetworkWriterFactory {

  public CompactBinaryTin() {
    super("Compact Binary TIN");
    addMediaTypeAndFileExtension("image/x-rs-compact-binary-tin", "tincd");
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
      return compactBinaryTinReader.read();
    }
  }

  @Override
  public TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Resource resource) {
    return new CompactBinaryTinWriter(resource);
  }

}
