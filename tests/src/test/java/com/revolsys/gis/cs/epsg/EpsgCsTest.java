package com.revolsys.gis.cs.epsg;


public class EpsgCsTest {
  //
  // private void printDms(
  // final Coordinate c1) {
  // printDms(c1.x);
  // System.out.print(", ");
  // printDms(c1.y);
  // System.out.println();
  // }
  //
  // private void printDms(
  // final double x) {
  // final int deg = (int)x;
  // final double minDouble = Math.abs(x - deg) * 60;
  // final int minInt = (int)minDouble;
  // final double sec = (minDouble - minInt) * 60;
  // System.out.print(deg);
  // System.out.print("o");
  // System.out.print(minInt);
  // System.out.print("'");
  // System.out.print(sec);
  // }
  //
  // public void testUTransverseMeractor() {
  // final ProjectedCoordinateSystem cs =
  // (ProjectedCoordinateSystem)EpsgCoordinateSystems.getCoordinateSystem(26910);
  // final TransverseMercator proj = new TransverseMercator(cs);
  // final DoubleCoordinates c1 = new DoubleCoordinates(-122, 49);
  // final DoubleCoordinates c2 = new DoubleCoordinates(2);
  // final DoubleCoordinates c3 = new DoubleCoordinates(-122, 49);
  // for (int i = 0; i < 1; i++) {
  // proj.project(c3, c2);
  // proj.inverse(c2, c3);
  // }
  // }
  //
  // public void testWktRead()
  // throws Exception {
  // int withProjectionCount = 0;
  // int withoutProjectionCount = 0;
  // final Map<Projection, Integer> counts = new HashMap<Projection, Integer>();
  // for (final Integer crsId : EpsgCoordinateSystems.getCoordinateSystemsById()
  // .keySet()) {
  // final CoordinateSystem cs =
  // EpsgCoordinateSystems.getCoordinateSystem(crsId);
  // if (cs instanceof ProjectedCoordinateSystem) {
  // final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)cs;
  // if (!projCs.isDeprecated()) {
  // if (projCs.getName().contains("British")) {
  // System.out.println(projCs.getAuthority());
  // }
  // final GeographicCoordinateSystem geoCs =
  // projCs.getGeographicCoordinateSystem();
  // final CoordinatesOperation inverseOperation =
  // ProjectionFactory.getCoordinatesOperation(
  // projCs, geoCs);
  // final CoordinatesOperation forwardOperation =
  // ProjectionFactory.getCoordinatesOperation(
  // geoCs, projCs);
  // if (inverseOperation == null) {
  // final Projection projection = projCs.getProjection();
  // Integer count = counts.get(projection);
  // if (count == null) {
  // count = 1;
  // } else {
  // count++;
  // }
  // counts.put(projection, count);
  // withoutProjectionCount++;
  // } else {
  // withProjectionCount++;
  // final Area area = projCs.getArea();
  // if (area != null) {
  // final Coordinate centre = area.getLatLonBounds().centre();
  // final Coordinate c2 = new Coordinate();
  // final Coordinate c3 = new Coordinate();
  // forwardOperation.perform(new CoordinateCoordinates(centre),
  // new CoordinateCoordinates(c2));
  // inverseOperation.perform(new CoordinateCoordinates(c2),
  // new CoordinateCoordinates(c3));
  // }
  // }
  // }
  //
  // }
  // }
  // }
  //
  // public double toDecimal(
  // final int deg,
  // final int min,
  // final double sec) {
  // return deg + min / 60.0 + sec / 3600.0;
  // }
  //
  // public void xtestGetCoordinateSystem()
  // throws Exception {
  // final ProjectedCoordinateSystem bcAlbers =
  // (ProjectedCoordinateSystem)EpsgCoordinateSystems.getCoordinateSystem(3005);
  // final ProjectedCoordinateSystem utm =
  // (ProjectedCoordinateSystem)EpsgCoordinateSystems.getCoordinateSystem(26910);
  //
  // final GeographicCoordinateSystem gcs1 =
  // bcAlbers.getGeographicCoordinateSystem();
  // final GeographicCoordinateSystem gcs2 =
  // utm.getGeographicCoordinateSystem();
  // System.out.println(gcs1.equals(gcs2));
  // }
  //
  // public void xtestTransverseMeractor() {
  // final ProjectedCoordinateSystem cs =
  // (ProjectedCoordinateSystem)EpsgCoordinateSystems.getCoordinateSystem(27700);
  // final TransverseMercator proj = new TransverseMercator(cs);
  // final Coordinate c1 = new Coordinate(toDecimal(0, 30, 0), toDecimal(50, 30,
  // 0));
  // printDms(c1);
  // final Coordinate c2 = new Coordinate();
  // final Coordinate c3 = new Coordinate();
  // final Coordinate c4 = new Coordinate(577274.99, 69740.50);
  // final Coordinate c5 = new Coordinate();
  // proj.project(new CoordinateCoordinates(c1), new CoordinateCoordinates(c2));
  // proj.inverse(new CoordinateCoordinates(c2), new CoordinateCoordinates(c3));
  // proj.inverse(new CoordinateCoordinates(c4), new CoordinateCoordinates(c5));
  // System.out.println(c1.distance(c3));
  // System.out.println(c1.distance(c5));
  // System.out.println(c2.distance(c4));
  // printDms(c1);
  // System.out.println(c2);
  // printDms(c3);
  // System.out.println(c4);
  // printDms(c5);
  // System.out.println();
  //
  // }
}
