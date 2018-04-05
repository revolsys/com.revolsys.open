package com.revolsys.geometry.cs;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.epsg.CoordinateOperation;
import com.revolsys.geometry.cs.projection.ChainedCoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.RadiansToDegreesOperation;
import com.revolsys.geometry.cs.projection.UnitConverstionOperation;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

import si.uom.NonSI;
import tec.uom.se.unit.Units;

public class GeographicCoordinateSystem extends AbstractCoordinateSystem {
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

  private static PrimeMeridian getPrimeMeridian(final GeodeticDatum datum) {
    if (datum == null) {
      return null;
    } else {
      return datum.getPrimeMeridian();
    }
  }

  private final AngularUnit angularUnit;

  private final GeodeticDatum geodeticDatum;

  private final PrimeMeridian primeMeridian;

  private CoordinateSystem sourceCoordinateSystem;

  private CoordinateOperation coordinateOperation;

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final List<Axis> axis, final Area area,
    final CoordinateSystem sourceCoordinateSystem, final CoordinateOperation coordinateOperation,
    final boolean deprecated) {
    this(id, name, geodeticDatum, getPrimeMeridian(geodeticDatum), axis, area,
      sourceCoordinateSystem, coordinateOperation, deprecated);
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit, final List<Axis> axis, final Authority authority) {
    super(id, name, axis, authority);
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final PrimeMeridian primeMeridian, final List<Axis> axis,
    final Area area, final CoordinateSystem sourceCoordinateSystem,
    final CoordinateOperation coordinateOperation, final boolean deprecated) {
    super(id, name, axis, area, deprecated);
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = (AngularUnit)axis.get(0).getUnit();
    this.sourceCoordinateSystem = sourceCoordinateSystem;
    this.coordinateOperation = coordinateOperation;
  }

  @Override
  public GeographicCoordinateSystem clone() {
    return (GeographicCoordinateSystem)super.clone();
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
      } else if (!equals(this.primeMeridian, cs.primeMeridian)) {
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

  @Override
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
      return equalsExact(geographicCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final GeographicCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!equals(this.angularUnit, cs.angularUnit)) {
        return false;
      } else if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (!equals(this.primeMeridian, cs.primeMeridian)) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  public AngularUnit getAngularUnit() {
    return this.angularUnit;
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
        if (angularUnit1.equals(Units.RADIAN)) {
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
      if (!linearUnit2.equals(Units.METRE)) {
        operations.add(new UnitConverstionOperation(Units.METRE, linearUnit2));
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
  public String getCoordinateSystemType() {
    return "Geographic";
  }

  public GeodeticDatum getDatum() {
    return this.geodeticDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    final Unit<Angle> unit = this.angularUnit.getUnit();
    final UnitConverter radianConverter = unit.getConverterTo(Units.RADIAN);

    final Ellipsoid ellipsoid = this.geodeticDatum.getEllipsoid();
    final double radius = ellipsoid.getSemiMajorAxis();
    final double radianFactor = radianConverter.convert(1);
    return Units.METRE.multiply(radius).multiply(radianFactor);
  }

  @Override
  public LinearUnit getLinearUnit() {

    final Ellipsoid ellipsoid = this.geodeticDatum.getEllipsoid();
    final double radius = ellipsoid.getSemiMajorAxis();
    final double radianFactor = this.angularUnit.toRadians(1);
    final double metres = radius * radianFactor;
    return new LinearUnit("custom", metres);
  }

  public PrimeMeridian getPrimeMeridian() {
    return this.primeMeridian;
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
  public BoundingBox newAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Area area = getArea();
    if (area != null) {
      return area.getLatLonBounds().convert(geometryFactory);
    } else {
      return geometryFactory.newBoundingBox(-180, -90, 180, 90);
    }
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    this.geodeticDatum.updateDigest(digest);
    this.primeMeridian.updateDigest(digest);
    this.angularUnit.updateDigest(digest);
  }
}
