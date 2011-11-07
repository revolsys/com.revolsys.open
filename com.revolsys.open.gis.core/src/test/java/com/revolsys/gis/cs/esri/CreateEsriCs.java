package com.revolsys.gis.cs.esri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.revolsys.gis.cs.AngularUnit;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.LinearUnit;
import com.revolsys.gis.cs.PrimeMeridian;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Projection;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;

public class CreateEsriCs {
  public static void copy(
    final InputStream in,
    final OutputStream out)
    throws IOException {
    final byte[] buffer = new byte[4906];
    int count;
    while ((count = in.read(buffer)) > -1) {
      out.write(buffer, 0, count);
    }
  }

  public static void main(
    final String[] args) {
    new CreateEsriCs().run();
  }

  private final Map<Integer, CoordinateSystem> csById = new HashMap<Integer, CoordinateSystem>();

  private PrintWriter geoCsOut;

  private final Map<CoordinateSystem, Integer> idByCs = new HashMap<CoordinateSystem, Integer>();

  private PrintWriter projCsOut;

  private final Map<String, Set<String>> projectionParameterNames = new HashMap<String, Set<String>>();

  public CreateEsriCs() {
    try {
      geoCsOut = new PrintWriter(
        new FileWriter(
          "src/main/resources/com/revolsys/gis/cs/esri/geographicCoordinateSystem.txt"));
      projCsOut = new PrintWriter(
        new FileWriter(
          "src/main/resources/com/revolsys/gis/cs/esri/projectedCoordinateSystem.txt"));
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private <T extends CoordinateSystem> T getEsriCs(
    final Integer id) {
    try {
      final File file = new File("c:/exports/data/esri/" + id + ".txt");
      if (!file.exists()) {

        final InputStream in = new URL("http://spatialreference.org/ref/epsg/"
          + id + "/esriwkt/").openStream();
        final OutputStream out = new FileOutputStream(file);
        try {
          copy(in, out);
        } finally {
          out.close();
          in.close();
        }

      }
      if (file.length() > 0) {
        final InputStream in = new FileInputStream(file);
        try {
          final CoordinateSystem cs = new WktCsParser(in).parse();
          return (T)cs;
        } finally {
          try {
            in.close();
          } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();

    }
    return null;
  }

  public void run() {
    final Map<Integer, CoordinateSystem> epsgCoordinateSystems = EpsgCoordinateSystems.getCoordinateSystemsById();
    for (final Entry<Integer, CoordinateSystem> entry : epsgCoordinateSystems.entrySet()) {
      final Integer id = entry.getKey();
      if (entry.getValue() instanceof GeographicCoordinateSystem) {
        try {
          final GeographicCoordinateSystem cs = getEsriCs(id);
          if (cs != null) {
            csById.put(id, cs);
            idByCs.put(cs, id);
            writeCs(id, cs);
          }
        } catch (final Throwable e) {
          e.printStackTrace();
        }
      }
    }
    for (final Entry<Integer, CoordinateSystem> entry : epsgCoordinateSystems.entrySet()) {
      final Integer id = entry.getKey();
      if (entry.getValue() instanceof ProjectedCoordinateSystem) {
        try {
          final ProjectedCoordinateSystem cs = getEsriCs(id);
          if (cs != null) {
            csById.put(id, cs);
            idByCs.put(cs, id);
            writeCs(id, cs);
          }
        } catch (final Throwable e) {
          e.printStackTrace();
        }
      }
    }

    geoCsOut.close();
    projCsOut.close();

  }

  private void writeCs(
    final Integer id,
    final GeographicCoordinateSystem cs) {
    geoCsOut.print(id);
    geoCsOut.print('\t');
    geoCsOut.print(cs.getName());
    geoCsOut.print('\t');
    final Datum datum = cs.getDatum();
    geoCsOut.print(datum.getName());
    geoCsOut.print('\t');
    final Spheroid spheroid = datum.getSpheroid();
    geoCsOut.print(spheroid.getName());
    geoCsOut.print('\t');
    geoCsOut.print(spheroid.getSemiMajorAxis());
    geoCsOut.print('\t');
    geoCsOut.print(spheroid.getInverseFlattening());
    geoCsOut.print('\t');
    final PrimeMeridian primeMeridian = cs.getPrimeMeridian();
    geoCsOut.print(primeMeridian.getName());
    geoCsOut.print('\t');
    geoCsOut.print(primeMeridian.getLongitude());
    geoCsOut.print('\t');
    final AngularUnit angularUnit = cs.getAngularUnit();
    geoCsOut.print(angularUnit.getName());
    geoCsOut.print('\t');
    geoCsOut.println(angularUnit.getConversionFactor());
  }

  private void writeCs(
    final Integer id,
    final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geoCs = cs.getGeographicCoordinateSystem();
    final Integer geoCsId = idByCs.get(geoCs);
    if (geoCsId != null) {
      projCsOut.print(id);
      projCsOut.print('\t');
      projCsOut.print(cs.getName());
      projCsOut.print('\t');
      projCsOut.print(geoCsId);
      final Projection projection = cs.getProjection();
      projCsOut.print('\t');
      if (projection != null) {
        final String projectionName = projection.getName();
        projCsOut.print(projectionName);
        final Set<String> parameterNames = projectionParameterNames.get(projectionName);
        final Set<String> currentParamNames = cs.getParameters().keySet();
        if (parameterNames == null) {
          projectionParameterNames.put(projectionName, currentParamNames);
        } else {
          if (!currentParamNames.equals(parameterNames)) {
            final Set<String> n = new HashSet<String>(currentParamNames);
            n.retainAll(parameterNames);
            System.out.println(projectionName + ":" + n);
          }
        }

      }
      projCsOut.print('\t');
      projCsOut.print(cs.getParameters());
      final LinearUnit unit = cs.getLinearUnit();
      projCsOut.print('\t');
      projCsOut.print(unit.getName());
      projCsOut.print('\t');
      projCsOut.println(unit.getConversionFactor());
    } else {
      System.err.println(id);
    }
  }
}
