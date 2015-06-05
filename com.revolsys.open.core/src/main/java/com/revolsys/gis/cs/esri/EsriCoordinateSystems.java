package com.revolsys.gis.cs.esri;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.Authority;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.CoordinateSystemParser;
import com.revolsys.gis.cs.CoordinateSystems;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.io.FileUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.spring.NonExistingResource;
import com.revolsys.spring.SpringUtil;

public class EsriCoordinateSystems {
  private static Map<CoordinateSystem, CoordinateSystem> coordinateSystems = new HashMap<CoordinateSystem, CoordinateSystem>();

  private static Map<Integer, CoordinateSystem> coordinateSystemsById = new HashMap<Integer, CoordinateSystem>();

  private static Map<String, CoordinateSystem> coordinateSystemsByName = new HashMap<String, CoordinateSystem>();

  static {
    final List<GeographicCoordinateSystem> geographicCoordinateSystems = CoordinateSystemParser.getGeographicCoordinateSystems(
      "ESRI",
      EsriCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/esri/geographicCoordinateSystem.txt"));
    for (final GeographicCoordinateSystem cs : geographicCoordinateSystems) {
      final int id = getCrsId(cs);
      coordinateSystemsById.put(id, cs);
      coordinateSystemsByName.put(cs.getName(), cs);
      coordinateSystems.put(cs, cs);
    }
    final List<ProjectedCoordinateSystem> projectedCoordinateSystems = CoordinateSystemParser.getProjectedCoordinateSystems(
      coordinateSystemsById,
      "ESRI",
      EsriCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/esri/projectedCoordinateSystem.txt"));
    for (final ProjectedCoordinateSystem cs : projectedCoordinateSystems) {
      final int id = getCrsId(cs);
      coordinateSystemsById.put(id, cs);
      coordinateSystemsByName.put(cs.getName(), cs);
      coordinateSystems.put(cs, cs);
    }
  }

  public static void createPrjFile(final File file,
    final GeometryFactory geometryFactory) {
    final FileSystemResource resource = new FileSystemResource(file);
    createPrjFile(resource, geometryFactory);
  }

  public static void createPrjFile(final Resource resource,
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final int srid = coordinateSystem.getId();
        final Resource prjResource = SpringUtil.getResourceWithExtension(
          resource, "prj");
        if (!(prjResource instanceof NonExistingResource)) {
          try (
            final OutputStream out = SpringUtil.getOutputStream(prjResource);
            final PrintWriter writer = new PrintWriter(
              FileUtil.createUtf8Writer(out));) {
            final CoordinateSystem esriCoordinateSystem = CoordinateSystems.getCoordinateSystem(new QName(
              "ESRI", String.valueOf(srid)));
            EsriCsWktWriter.write(writer, esriCoordinateSystem, -1);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(EsriCoordinateSystems.class).error(
              "Unable to create: " + resource, e);
          }
        }
      }
    }
  }

  public static CoordinateSystem getCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return null;
    } else {
      CoordinateSystem coordinateSystem2 = coordinateSystemsByName.get(coordinateSystem.getName());
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
   * Create a geometry factory from a .prj with the same base name as the resource if it exists. Returns null if the prj file does not exist.
   * @param resource
   * @return
   */
  public static GeometryFactory getGeometryFactory(final Resource resource) {
    final Resource projResource = SpringUtil.getResourceWithExtension(resource,
      "prj");
    if (projResource.exists()) {
      try {
        final CoordinateSystem coordinateSystem = getCoordinateSystem(projResource);
        final int srid = EsriCoordinateSystems.getCrsId(coordinateSystem);
        if (srid > 0 && srid < 2000000) {
          return GeometryFactory.floating(srid, 2);
        } else {
          return GeometryFactory.fixed(coordinateSystem, 2, -1);
        }
      } catch (final Exception e) {
        LoggerFactory.getLogger(EsriCoordinateSystems.class).error(
          "Unable to load projection from " + projResource);
      }
    }
    return null;
  }

}
