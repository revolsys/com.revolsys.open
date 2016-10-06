package com.revolsys.elevation.tin.tin;

import java.io.BufferedReader;
import java.util.Map;

import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReadFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriterFactory;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class Tin extends AbstractIoFactoryWithCoordinateSystem
  implements TriangulatedIrregularNetworkReadFactory, TriangulatedIrregularNetworkWriterFactory {
  public Tin() {
    super("ASCII TIN");
    addMediaTypeAndFileExtension("image/x-tin", "tin");
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      BufferedReader reader = resource.newBufferedReader()) {
      GeometryFactory geometryFactory = EsriCoordinateSystems.getGeometryFactory(resource);
      if (geometryFactory == null) {
        geometryFactory = Maps.get(properties, TriangulatedIrregularNetwork.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.DEFAULT;
        }
      }
      try (
        TinReader tinReader = new TinReader(geometryFactory, resource)) {
        return tinReader.read();
      }
    } catch (final Throwable e) {
      throw Exceptions.wrap("Error reading: " + resource, e);
    }
  }

  @Override
  public TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Resource resource) {
    return new TinWriter(resource);
  }

}
