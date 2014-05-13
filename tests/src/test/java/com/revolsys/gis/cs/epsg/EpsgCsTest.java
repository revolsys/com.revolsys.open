package com.revolsys.gis.cs.epsg;


public class EpsgCsTest {
  //
  // private void printDms(
  // final Point c1) {
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
  // final ProjectedCoordinatesSystem cs =
  // (ProjectedCoordinatesSystem)EpsgCoordinatesSystems.getCoordinatesSystem(26910);
  // final TransverseMercator proj = new TransverseMercator(cs);
  // final DoubleCoordinatess c1 = new DoubleCoordinatess(-122, 49);
  // final DoubleCoordinatess c2 = new DoubleCoordinatess(2);
  // final DoubleCoordinatess c3 = new DoubleCoordinatess(-122, 49);
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
  // for (final Integer crsId : EpsgCoordinatesSystems.getCoordinatesSystemsById()
  // .keySet()) {
  // final CoordinatesSystem cs =
  // EpsgCoordinatesSystems.getCoordinatesSystem(crsId);
  // if (cs instanceof ProjectedCoordinatesSystem) {
  // final ProjectedCoordinatesSystem projCs = (ProjectedCoordinatesSystem)cs;
  // if (!projCs.isDeprecated()) {
  // if (projCs.getName().contains("British")) {
  // System.out.println(projCs.getAuthority());
  // }
  // final GeographicCoordinatesSystem geoCs =
  // projCs.getGeographicCoordinatesSystem();
  // final CoordinatessOperation inverseOperation =
  // ProjectionFactory.getCoordinatessOperation(
  // projCs, geoCs);
  // final CoordinatessOperation forwardOperation =
  // ProjectionFactory.getCoordinatessOperation(
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
  // final Point centre = area.getLatLonBounds().centre();
  // final Point c2 = new Coordinates();
  // final Point c3 = new Coordinates();
  // forwardOperation.perform(new CoordinatesCoordinatess(centre),
  // new CoordinatesCoordinatess(c2));
  // inverseOperation.perform(new CoordinatesCoordinatess(c2),
  // new CoordinatesCoordinatess(c3));
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
  // public void xtestGetCoordinatesSystem()
  // throws Exception {
  // final ProjectedCoordinatesSystem bcAlbers =
  // (ProjectedCoordinatesSystem)EpsgCoordinatesSystems.getCoordinatesSystem(3005);
  // final ProjectedCoordinatesSystem utm =
  // (ProjectedCoordinatesSystem)EpsgCoordinatesSystems.getCoordinatesSystem(26910);
  //
  // final GeographicCoordinatesSystem gcs1 =
  // bcAlbers.getGeographicCoordinatesSystem();
  // final GeographicCoordinatesSystem gcs2 =
  // utm.getGeographicCoordinatesSystem();
  // System.out.println(gcs1.equals(gcs2));
  // }
  //
  // public void xtestTransverseMeractor() {
  // final ProjectedCoordinatesSystem cs =
  // (ProjectedCoordinatesSystem)EpsgCoordinatesSystems.getCoordinatesSystem(27700);
  // final TransverseMercator proj = new TransverseMercator(cs);
  // final Point c1 = new Coordinates((double)toDecimal(0, 30, 0), toDecimal(50, 30,
  // 0));
  // printDms(c1);
  // final Point c2 = new Coordinates();
  // final Point c3 = new Coordinates();
  // final Point c4 = new Coordinates((double)577274.99, 69740.50);
  // final Point c5 = new Coordinates();
  // proj.project(new CoordinatesCoordinatess(c1), new CoordinatesCoordinatess(c2));
  // proj.inverse(new CoordinatesCoordinatess(c2), new CoordinatesCoordinatess(c3));
  // proj.inverse(new CoordinatesCoordinatess(c4), new CoordinatesCoordinatess(c5));
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
