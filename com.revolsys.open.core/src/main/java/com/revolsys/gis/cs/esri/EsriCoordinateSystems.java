package com.revolsys.gis.cs.esri;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.Authority;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.CoordinateSystemParser;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.WktCsParser;

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

}
