package com.revolsys.gis.cs.esri;

import java.io.File;
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

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.WktCsParser;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.spring.resource.PathResource;

public class CreateEsriCs {
  public static void copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[4906];
    int count;
    while ((count = in.read(buffer)) > -1) {
      out.write(buffer, 0, count);
    }
  }

  public static void main(final String[] args) {
    new CreateEsriCs().run();
  }

  private final Map<Integer, CoordinateSystem> csById = new HashMap<Integer, CoordinateSystem>();

  private PrintWriter geoCsOut;

  private final Map<CoordinateSystem, Integer> idByCs = new HashMap<CoordinateSystem, Integer>();

  private PrintWriter projCsOut;

  private final Map<String, Set<ParameterName>> projectionParameterNames = new HashMap<>();

  public CreateEsriCs() {
    try {
      this.geoCsOut = new PrintWriter(new FileWriter(
        "src/main/resources/com/revolsys/gis/cs/esri/geographicCoordinateSystem.txt"));
      this.projCsOut = new PrintWriter(new FileWriter(
        "src/main/resources/com/revolsys/gis/cs/esri/projectedCoordinateSystem.txt"));
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private <T extends CoordinateSystem> T getEsriCs(final Integer id) {
    try {
      final File file = new File("c:/exports/data/esri/" + id + ".txt");
      if (!file.exists()) {

        final InputStream in = new URL("http://spatialreference.org/ref/epsg/" + id + "/esriwkt/")
          .openStream();
        final OutputStream out = new FileOutputStream(file);
        try {
          copy(in, out);
        } finally {
          out.close();
          in.close();
        }

      }
      if (file.length() > 0) {
        final CoordinateSystem cs = WktCsParser.read(new PathResource(file));
        return (T)cs;
      }
    } catch (final IOException e) {
      e.printStackTrace();

    }
    return null;
  }

  public void run() {
    final Map<Integer, CoordinateSystem> epsgCoordinateSystems = EpsgCoordinateSystems
      .getCoordinateSystemsById();
    for (final Entry<Integer, CoordinateSystem> entry : epsgCoordinateSystems.entrySet()) {
      final Integer id = entry.getKey();
      if (entry.getValue() instanceof GeographicCoordinateSystem) {
        try {
          final GeographicCoordinateSystem cs = getEsriCs(id);
          if (cs != null) {
            this.csById.put(id, cs);
            this.idByCs.put(cs, id);
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
            this.csById.put(id, cs);
            this.idByCs.put(cs, id);
            writeCs(id, cs);
          }
        } catch (final Throwable e) {
          e.printStackTrace();
        }
      }
    }

    this.geoCsOut.close();
    this.projCsOut.close();

  }

  private void writeCs(final Integer id, final GeographicCoordinateSystem cs) {
    this.geoCsOut.print(id);
    this.geoCsOut.print('\t');
    this.geoCsOut.print(cs.getCoordinateSystemName());
    this.geoCsOut.print('\t');
    final GeodeticDatum datum = cs.getDatum();
    this.geoCsOut.print(datum.getName());
    this.geoCsOut.print('\t');
    final Ellipsoid spheroid = datum.getEllipsoid();
    this.geoCsOut.print(spheroid.getName());
    this.geoCsOut.print('\t');
    this.geoCsOut.print(spheroid.getSemiMajorAxis());
    this.geoCsOut.print('\t');
    this.geoCsOut.print(spheroid.getInverseFlattening());
    this.geoCsOut.print('\t');
    final PrimeMeridian primeMeridian = cs.getPrimeMeridian();
    this.geoCsOut.print(primeMeridian.getName());
    this.geoCsOut.print('\t');
    this.geoCsOut.print(primeMeridian.getLongitude());
    this.geoCsOut.print('\t');
    final AngularUnit angularUnit = cs.getAngularUnit();
    this.geoCsOut.print(angularUnit.getName());
    this.geoCsOut.print('\t');
    this.geoCsOut.println(angularUnit.getConversionFactor());
  }

  private void writeCs(final Integer id, final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geoCs = cs.getGeographicCoordinateSystem();
    final Integer geoCsId = this.idByCs.get(geoCs);
    if (geoCsId != null) {
      this.projCsOut.print(id);
      this.projCsOut.print('\t');
      this.projCsOut.print(cs.getCoordinateSystemName());
      this.projCsOut.print('\t');
      this.projCsOut.print(geoCsId);
      final CoordinatesProjection projection = cs.getCoordinatesProjection();
      this.projCsOut.print('\t');
      if (projection != null) {
        final String projectionName = projection.toString();
        this.projCsOut.print(projectionName);
        final Set<ParameterName> parameterNames = this.projectionParameterNames.get(projectionName);
        final Set<ParameterName> currentParamNames = cs.getParameters().keySet();
        if (parameterNames == null) {
          this.projectionParameterNames.put(projectionName, currentParamNames);
        } else {
          if (!currentParamNames.equals(parameterNames)) {
            final Set<ParameterName> n = new HashSet<>(currentParamNames);
            n.retainAll(parameterNames);
            System.out.println(projectionName + ":" + n);
          }
        }

      }
      this.projCsOut.print('\t');
      this.projCsOut.print(cs.getParameters());
      final LinearUnit unit = cs.getLinearUnit();
      this.projCsOut.print('\t');
      this.projCsOut.print(unit.getName());
      this.projCsOut.print('\t');
      this.projCsOut.println(unit.getConversionFactor());
    } else {
      System.err.println(id);
    }
  }
}
