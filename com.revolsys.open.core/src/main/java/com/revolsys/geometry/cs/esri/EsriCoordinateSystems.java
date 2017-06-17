package com.revolsys.geometry.cs.esri;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.CoordinateSystemParser;
import com.revolsys.geometry.cs.CoordinateSystems;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.WktCsParser;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;

public class EsriCoordinateSystems {
  private static Map<CoordinateSystem, CoordinateSystem> coordinateSystems = new HashMap<>();

  private static Map<Integer, CoordinateSystem> coordinateSystemsById = new HashMap<>();

  private static Map<String, CoordinateSystem> coordinateSystemsByName = new HashMap<>();

  static {
    try {
      final List<GeographicCoordinateSystem> geographicCoordinateSystems = CoordinateSystemParser
        .getGeographicCoordinateSystems("ESRI", EpsgCoordinateSystems.newReader(
          EsriCoordinateSystems.class, "/com/revolsys/gis/cs/esri/geographicCoordinateSystem.txt"));
      for (final GeographicCoordinateSystem cs : geographicCoordinateSystems) {
        final int id = getCrsId(cs);
        coordinateSystemsById.put(id, cs);
        coordinateSystemsByName.put(cs.getCoordinateSystemName(), cs);
        coordinateSystems.put(cs, cs);
      }
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = CoordinateSystemParser
        .getProjectedCoordinateSystems(coordinateSystemsById, "ESRI",
          EpsgCoordinateSystems.newReader(EsriCoordinateSystems.class,
            "/com/revolsys/gis/cs/esri/projectedCoordinateSystem.txt"));
      for (final ProjectedCoordinateSystem cs : projectedCoordinateSystems) {
        final int id = getCrsId(cs);
        coordinateSystemsById.put(id, cs);
        coordinateSystemsByName.put(cs.getCoordinateSystemName(), cs);
        coordinateSystems.put(cs, cs);
      }
    } catch (final IOException e) {
      Logs.error(EsriCoordinateSystems.class, e);
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
        if (coordinateSystem != null) {
          final int srid = EsriCoordinateSystems.getCrsId(coordinateSystem);
          if (srid > 0 && srid < 2000000) {
            return GeometryFactory.floating(srid, 2);
          } else {
            return GeometryFactory.fixed(coordinateSystem, 2, -1);
          }
        }
      } catch (final Exception e) {
        Logs.error(EsriCoordinateSystems.class, "Unable to load projection from " + projResource,
          e);
      }
    }
    return null;
  }

  public static void writePrjFile(final Object target, final GeometryFactory geometryFactory) {
    final Resource resource = Resource.getResource(target);

    if (geometryFactory != null && resource != null) {
      final Resource prjResource = resource.newResourceChangeExtension("prj");
      if (prjResource != null && geometryFactory.isHasCoordinateSystem()) {
        try (
          final Writer writer = prjResource.newWriter(StandardCharsets.ISO_8859_1)) {
          writePrjFile(writer, geometryFactory);
        } catch (final Throwable e) {
          Logs.error(EsriCoordinateSystems.class, "Unable to create: " + resource, e);
        }
      }
    }
  }

  protected static boolean writePrjFile(final Writer writer,
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final int srid = coordinateSystem.getCoordinateSystemId();
        final CoordinateSystem esriCoordinateSystem = CoordinateSystems
          .getCoordinateSystem(new QName("ESRI", String.valueOf(srid)));
        if (esriCoordinateSystem == null) {
          EsriCsWktWriter.write(writer, coordinateSystem, -1);
        } else {
          EsriCsWktWriter.write(writer, esriCoordinateSystem, -1);
        }
        return true;
      }
    }
    return false;
  }

}
