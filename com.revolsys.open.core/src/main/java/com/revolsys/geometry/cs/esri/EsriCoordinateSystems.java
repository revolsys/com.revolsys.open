package com.revolsys.geometry.cs.esri;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;

import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.CoordinateSystemParser;
import com.revolsys.geometry.cs.CoordinateSystems;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.WktCsParser;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.Paths;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.WrappedException;

public class EsriCoordinateSystems {
  private static Map<CoordinateSystem, CoordinateSystem> coordinateSystems = new HashMap<CoordinateSystem, CoordinateSystem>();

  private static Map<Integer, CoordinateSystem> coordinateSystemsById = new HashMap<Integer, CoordinateSystem>();

  private static Map<String, CoordinateSystem> coordinateSystemsByName = new HashMap<String, CoordinateSystem>();

  static {
    final List<GeographicCoordinateSystem> geographicCoordinateSystems = CoordinateSystemParser
      .getGeographicCoordinateSystems("ESRI", EsriCoordinateSystems.class
        .getResourceAsStream("/com/revolsys/gis/cs/esri/geographicCoordinateSystem.txt"));
    for (final GeographicCoordinateSystem cs : geographicCoordinateSystems) {
      final int id = getCrsId(cs);
      coordinateSystemsById.put(id, cs);
      coordinateSystemsByName.put(cs.getCoordinateSystemName(), cs);
      coordinateSystems.put(cs, cs);
    }
    final List<ProjectedCoordinateSystem> projectedCoordinateSystems = CoordinateSystemParser
      .getProjectedCoordinateSystems(coordinateSystemsById, "ESRI", EsriCoordinateSystems.class
        .getResourceAsStream("/com/revolsys/gis/cs/esri/projectedCoordinateSystem.txt"));
    for (final ProjectedCoordinateSystem cs : projectedCoordinateSystems) {
      final int id = getCrsId(cs);
      coordinateSystemsById.put(id, cs);
      coordinateSystemsByName.put(cs.getCoordinateSystemName(), cs);
      coordinateSystems.put(cs, cs);
    }
  }

  public static void createPrjFile(final File file, final GeometryFactory geometryFactory) {
    final Path path = file.toPath();
    createPrjFile(path, geometryFactory);
  }

  public static void createPrjFile(final Path path, final GeometryFactory geometryFactory) {
    if (path != null) {
      final Path prjPath = Paths.withExtension(path, "prj");
      try (
        final Writer writer = Files.newBufferedWriter(prjPath, StandardCharsets.ISO_8859_1)) {
        createPrjFile(writer, geometryFactory);
      } catch (final IOException e) {
        throw new WrappedException(e);
      }
    }
  }

  public static void createPrjFile(final Resource resource, final GeometryFactory geometryFactory) {
    final Resource prjResource = resource.newResourceChangeExtension("prj");
    if (prjResource != null) {
      try (
        final Writer writer = prjResource.newWriter(StandardCharsets.ISO_8859_1)) {
        createPrjFile(writer, geometryFactory);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(EsriCoordinateSystems.class).error("Unable to create: " + resource,
          e);
      }
    }
  }

  protected static void createPrjFile(final Writer writer, final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final int srid = coordinateSystem.getCoordinateSystemId();
        final CoordinateSystem esriCoordinateSystem = CoordinateSystems
          .getCoordinateSystem(new QName("ESRI", String.valueOf(srid)));
        EsriCsWktWriter.write(writer, esriCoordinateSystem, -1);
      }
    }
  }

  public static CoordinateSystem getCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return null;
    } else {
      CoordinateSystem coordinateSystem2 = coordinateSystemsByName
        .get(coordinateSystem.getCoordinateSystemName());
      if (coordinateSystem2 == null) {
        coordinateSystem2 = coordinateSystems.get(coordinateSystem);
        if (coordinateSystem2 == null) {
          return coordinateSystem;
        }
      }
      return coordinateSystem2;
    }
  }

  public static CoordinateSystem getCoordinateSystem(final File file) {
    return getCoordinateSystem(new FileSystemResource(file));
  }

  public static CoordinateSystem getCoordinateSystem(final int crsId) {
    final CoordinateSystem coordinateSystem = coordinateSystemsById.get(crsId);
    return coordinateSystem;
  }

  public static CoordinateSystem getCoordinateSystem(final Resource resource) {
    final WktCsParser parser = new WktCsParser(resource);
    return getCoordinateSystem(parser);
  }

  public static CoordinateSystem getCoordinateSystem(final String wkt) {
    final WktCsParser parser = new WktCsParser(wkt);
    return getCoordinateSystem(parser);
  }

  public static CoordinateSystem getCoordinateSystem(final WktCsParser parser) {
    final CoordinateSystem coordinateSystem = parser.parse();
    return getCoordinateSystem(coordinateSystem);
  }

  public static int getCrsId(final CoordinateSystem coordinateSystem) {
    final Authority authority = coordinateSystem.getAuthority();
    if (authority != null) {
      final String name = authority.getName();
      final String code = authority.getCode();
      if (name.equals("ESRI")) {
        return Integer.parseInt(code);
      }
    }
    return 0;
  }

  /**
   * Construct a new geometry factory from a .prj with the same base name as the resource if it exists. Returns null if the prj file does not exist.
   * @param resource
   * @return
   */
  public static GeometryFactory getGeometryFactory(final Resource resource) {
    final Resource projResource = resource.newResourceChangeExtension("prj");
    if (Resource.exists(projResource)) {
      try {
        final CoordinateSystem coordinateSystem = getCoordinateSystem(projResource);
        final int srid = EsriCoordinateSystems.getCrsId(coordinateSystem);
        if (srid > 0 && srid < 2000000) {
          return GeometryFactory.floating(srid, 2);
        } else {
          return GeometryFactory.fixed(coordinateSystem, 2, -1);
        }
      } catch (final Exception e) {
        LoggerFactory.getLogger(EsriCoordinateSystems.class)
          .error("Unable to load projection from " + projResource);
      }
    }
    return null;
  }

}
