package com.revolsys.gis.geometry.io;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.IoFactoryRegistry;

public abstract class AbstractGeometryReaderFactory extends AbstractIoFactory
  implements GeometryReaderFactory {
  public static GeometryReader geometryReader(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeometryReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      GeometryReaderFactory.class, resource);
    if (readerFactory == null) {
      return null;
    } else {
      final GeometryReader reader = readerFactory.createGeometryReader(resource);
      return reader;
    }
  }

  private final boolean binary;

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractGeometryReaderFactory(final String name, final boolean binary) {
    super(name);
    this.binary = binary;
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  @Override
  public boolean isBinary() {
    return binary;
  }

  @Override
  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return coordinateSystems.contains(coordinateSystem);
  }

  protected void setCoordinateSystems(
    final CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<CoordinateSystem>(
      Arrays.asList(coordinateSystems)));
  }

  protected void setCoordinateSystems(
    final Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }
}
