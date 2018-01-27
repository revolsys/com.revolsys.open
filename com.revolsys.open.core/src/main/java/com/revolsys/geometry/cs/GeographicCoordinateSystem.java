package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.epsg.CoordinateOperation;
import com.revolsys.geometry.cs.epsg.EpsgAuthority;
import com.revolsys.geometry.cs.projection.ChainedCoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.RadiansToDegreesOperation;
import com.revolsys.geometry.cs.projection.UnitConverstionOperation;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class GeographicCoordinateSystem implements CoordinateSystem {
  public static final double EARTH_RADIUS = 6378137;

  private static final long serialVersionUID = 8655274386401351222L;

  public static double distanceMetres(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final double lon1Radians = Math.toRadians(lon1);
    final double lon2Radians = Math.toRadians(lon2);
    final double width = lon2Radians - lon1Radians;

    final double lat1Radians = Math.toRadians(lat1);
    final double lat2Radians = Math.toRadians(lat2);

    final double height = lat2Radians - lat1Radians;

    final double sinHeightOver2 = Math.sin(height / 2);
    final double sinWidthOver2 = Math.sin(width / 2);
    final double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(sinHeightOver2 * sinHeightOver2
      + Math.cos(lat1Radians) * Math.cos(lat2Radians) * sinWidthOver2 * sinWidthOver2));
    return distance;
  }

  private final AngularUnit angularUnit;

  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<>();

  private final GeodeticDatum geodeticDatum;

  private boolean deprecated;

  private final int id;

  private final String name;

  private final PrimeMeridian primeMeridian;

  private CoordinateSystem sourceCoordinateSystem;

  private CoordinateOperation coordinateOperation;

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final List<Axis> axis, final Area area,
    final CoordinateSystem sourceCoordinateSystem, final CoordinateOperation coordinateOperation,
    final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = null;
    this.angularUnit = (AngularUnit)axis.get(0).getUnit();
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.authority = new EpsgAuthority(id);
    this.sourceCoordinateSystem = sourceCoordinateSystem;
    this.coordinateOperation = coordinateOperation;
    this.deprecated = deprecated;
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit, final List<Axis> axis, final Area area,
    final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit, final List<Axis> axis, final Authority authority) {
    this.id = id;
    this.name = name;
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.authority = authority;
  }

  @Override
  public GeographicCoordinateSystem clone() {
    try {
      return (GeographicCoordinateSystem)super.clone();
    } catch (final Exception e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)object;
      if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else if (!equals(this.angularUnit, cs.angularUnit)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean equals(final Object object1, final Object object2) {
    if (object1 == object2) {
      return true;
    } else if (object1 == null || object2 == null) {
      return false;
    } else {
      return object1.equals(object2);
    }
  }

  public boolean equalsExact(final GeographicCoordinateSystem cs) {
    if (cs == null) {
      return false;
    } else if (cs == this) {
      return true;
    } else {
      if (!equals(this.angularUnit, cs.angularUnit)) {
        return false;
      } else if (!equals(this.area, cs.area)) {
        return false;
      } else if (!equals(this.authority, cs.authority)) {
        return false;
      } else if (!equals(this.axis, cs.axis)) {
        return false;
      } else if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (this.deprecated != cs.deprecated) {
        return false;
      } else if (this.id != cs.id) {
        return false;
      } else if (!equals(this.name, cs.name)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else {
        return true;
      }
    }
  }

  public AngularUnit getAngularUnit() {
    return this.angularUnit;
  }

  @Override
  public Area getArea() {
    return this.area;
  }

  @Override
  public BoundingBox getAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (this.area != null) {
      return this.area.getLatLonBounds().convert(geometryFactory);
    } else {
      return geometryFactory.newBoundingBox(-180, -90, 180, 90);
    }
  }

  @Override
  public Authority getAuthority() {
    return this.authority;
  }

  @Override
  public List<Axis> getAxis() {
    return this.axis;
  }

  public CoordinateOperation getCoordinateOperation() {
    return this.coordinateOperation;
  }

  @Override
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null || this == coordinateSystem) {
      return null;
    } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
      final Unit<Angle> angularUnit1 = getUnit();

      // TODO GeodeticDatum shift
      final Unit<Angle> angularUnit2 = geographicCoordinateSystem.getUnit();
      if (!angularUnit1.equals(angularUnit2)) {
        return new UnitConverstionOperation(angularUnit1, angularUnit2, 2);
      } else {
        return null;
      }
    } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final List<CoordinatesOperation> operations = new ArrayList<>();
      final Unit<Angle> angularUnit1 = getUnit();
      if (!angularUnit1.equals(NonSI.DEGREE_ANGLE)) {
        CoordinatesOperation converstionOperation;
        if (angularUnit1.equals(SI.RADIAN)) {
          converstionOperation = RadiansToDegreesOperation.INSTANCE;
        } else {
          converstionOperation = new UnitConverstionOperation(angularUnit1, NonSI.DEGREE_ANGLE, 2);
        }

        operations.add(converstionOperation);
      }
      // TODO geodeticDatum shift
      final CoordinatesOperation projectOperation = projectedCoordinateSystem
        .getProjectCoordinatesOperation();
      if (projectOperation != null) {
        operations.add(projectOperation);
      }
      final Unit<Length> linearUnit2 = projectedCoordinateSystem.getLengthUnit();
      if (!linearUnit2.equals(SI.METRE)) {
        operations.add(new UnitConverstionOperation(SI.METRE, linearUnit2));
      }
      switch (operations.size()) {
        case 0:
          return null;
        case 1:
          return operations.get(0);
        default:
          return new ChainedCoordinatesOperation(operations);
      }
    } else {
      return null;
    }
  }

  @Override
  public int getCoordinateSystemId() {
    return this.id;
  }

  @Override
  public String getCoordinateSystemName() {
    return this.name;
  }

  public GeodeticDatum getDatum() {
    return this.geodeticDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    final Unit<Angle> unit = this.angularUnit.getUnit();
    final UnitConverter radianConverter = unit.getConverterTo(SI.RADIAN);

    final Spheroid spheroid = this.geodeticDatum.getSpheroid();
    final double radius = spheroid.getSemiMajorAxis();
    final double radianFactor = radianConverter.convert(1);
    return SI.METRE.times(radius).times(radianFactor);
  }

  public PrimeMeridian getPrimeMeridian() {
    if (this.primeMeridian == null) {
      if (this.geodeticDatum == null) {
        return null;
      } else {
        return this.geodeticDatum.getPrimeMeridian();
      }
    } else {
      return this.primeMeridian;
    }
  }

  public CoordinateSystem getSourceCoordinateSystem() {
    return this.sourceCoordinateSystem;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Unit<Angle> getUnit() {
    return this.angularUnit.getUnit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.geodeticDatum != null) {
      result = prime * result + this.geodeticDatum.hashCode();
    }
    if (getPrimeMeridian() != null) {
      result = prime * result + getPrimeMeridian().hashCode();
    }
    return result;
  }

  @Override
  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
