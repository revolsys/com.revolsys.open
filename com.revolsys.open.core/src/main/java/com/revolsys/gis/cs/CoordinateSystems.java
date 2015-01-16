package com.revolsys.gis.cs;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;

public class CoordinateSystems {
  public static CoordinateSystem getCoordinateSystem(
    final QName coordinateSystemId) {
    if (coordinateSystemId != null) {
      final String authority = coordinateSystemId.getNamespaceURI();
      final String srid = coordinateSystemId.getLocalPart();
      if (srid.trim().length() > 0) {
        try {
          final Integer id = Integer.valueOf(srid);
          if (authority.equals("") || authority.equals("EPSG")) {
            return EpsgCoordinateSystems.getCoordinateSystem(id);
          } else if (authority == null || authority.equals("")
              || authority.equals("ESRI")) {
            return EsriCoordinateSystems.getCoordinateSystem(id);
          }
        } catch (final NumberFormatException e) {
        }
      }
    }
    return null;
  }
}
