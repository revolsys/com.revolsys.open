package com.revolsys.evelvation.tin.test;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.ValueHolder;
import com.revolsys.elevation.cloud.las.LasPoint0Core;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasReader;
import com.revolsys.elevation.tin.SimpleTriangulatedIrregularNetworkBuilder;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.spring.resource.PathResource;
//import com.revolsys.swing.component.BasePanel;
//import com.revolsys.swing.parallel.Invoke;

public class TriangulationVisualization {

  public static void tinVisual() {
    final GeometryFactory geometryFactory = GeometryFactory.floating(3005, 3);

    final List<Point> points = new ArrayList<>();
    points.add(geometryFactory.point(5, 0, 1));
    points.add(geometryFactory.point(10, 5, 1));
    points.add(geometryFactory.point(5, 10, 1));
    points.add(geometryFactory.point(0, 5, 1));

    tinVisual(geometryFactory, points);

  }
  //
  // public static void testDem() throws IOException {
  // final GeometryFactory geometryFactory = GeometryFactory.fixed(3005, 1000.0);
  // final GbaDemConfig config = new GbaDemConfig();
  // final Path basePath = config.getBasePath();
  // final boolean processAll = false;
  // final RecordDefinition extentRecordDefinition = new RecordDefinitionBuilder("extents") //
  // .addField("LETTER_BLOCK", DataTypes.STRING) //
  // .addField("POLYGON", DataTypes.POLYGON) //
  // .setGeometryFactory(geometryFactory) //
  // .getRecordDefinition();
  // final Path trim25kDirectory = config.getTrim25mAlbersDirectory();
  // RecordWriter writer = null;
  // if (processAll) {
  // writer = RecordWriter.newRecordWriter(extentRecordDefinition,
  // trim25kDirectory.resolve("extents.shp"));
  // }
  // try (
  // RecordWriter writer2 = writer) {
  //
  // Files.walk(trim25kDirectory).forEach((demZipFile) -> {
  // final String baseName = Paths.getBaseName(demZipFile);
  // if (processAll || baseName.equals("92i-albers-elevation.asc")) {
  // try {
  // final String fileNameExtension = Paths.getFileNameExtension(demZipFile);
  // if (fileNameExtension.equals("zip")) {
  // final FileSystem fileSystem = FileSystems.newFileSystem(demZipFile, null);
  // final String letterBlock = baseName.substring(0, baseName.indexOf('-'));
  // final Path demFile = fileSystem.getPath(baseName);
  //
  // final Map<String, Object> properties = new HashMap<>();
  // properties.put(GriddedElevationModel.GEOMETRY_FACTORY, geometryFactory);
  // properties.put(EsriAsciiGriddedElevation.PROPERTY_READ_DATA, !processAll);
  //
  // final GriddedElevationModel sourceModel = GriddedElevationModel
  // .newGriddedElevationModel(demFile, properties);
  // final BoundingBox sourceBoundingBox = sourceModel.getBoundingBox();
  // if (!processAll) {
  // for (final RectangularMapTile targetTile : GbaDemConfig.GRID_25m
  // .getTiles(sourceBoundingBox)) {
  // final BoundingBox targetBoundingBox = targetTile.getBoundingBox();
  // final boolean contained = sourceBoundingBox.covers(targetBoundingBox);
  // if (contained) {
  // System.out.println(targetTile);
  // } else {
  // System.err.println(targetTile);
  // }
  // final Path targetPath = config.getGriddedElevationModelPath(3005, 25, targetTile,
  // "asc");
  // final GriddedElevationModel targetModel = config.getGriddedElevationModel(3005,
  // 25, targetTile, "asc");
  // final int targetHeight = targetModel.getHeight();
  // final int targetWidth = targetModel.getWidth();
  // for (int targetJ = 0; targetJ < targetHeight; targetJ++) {
  // for (int targetI = 0; targetI < targetWidth; targetI++) {
  // final double x = targetModel.getX(targetI);
  // final double y = targetModel.getY(targetJ);
  // final int sourceI = sourceModel.getCellX(x);
  // final int sourceJ = sourceModel.getCellY(y);
  // if (!sourceModel.isNull(sourceI, sourceJ)) {
  // final short elevation = sourceModel.getElevationShort(sourceI, sourceJ);
  // targetModel.setElevation(targetI, targetJ, elevation);
  // }
  // }
  // }
  // targetModel.writeGriddedElevationModel(targetPath);
  // }
  // }
  //
  // if (writer2 != null) {
  // final Record record = writer2.newRecord();
  // record.setValue("LETTER_BLOCK", letterBlock);
  // final BoundingBox boundingBox = sourceModel.getBoundingBox();
  // record.setGeometryValue(boundingBox.toPolygon(1));
  // writer2.write(record);
  // }
  // }
  // } catch (final Exception e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }
  // });
  // }
  // }

  public static void tinVisual(final GeometryFactory geometryFactory, final List<Point> points) {
    final SimpleTriangulatedIrregularNetworkBuilder tin = new SimpleTriangulatedIrregularNetworkBuilder(
      geometryFactory);
    for (final Point point : points) {
      tin.insertVertex(point);
    }

    BoundingBox boundingBox = tin.getBoundingBox();
    double mapWidth = boundingBox.getWidth() + 4;
    double mapHeight = boundingBox.getHeight() + 4;
    if (mapHeight > mapWidth) {
      boundingBox = boundingBox.expand((mapHeight - mapWidth) / 2, 0);
      mapWidth = boundingBox.getWidth();
    } else if (mapHeight < mapWidth) {
      boundingBox = boundingBox.expand(0, (mapWidth - mapHeight) / 2);
      mapHeight = boundingBox.getHeight();
    }

    final AffineTransform transform = new AffineTransform();
    final double pixelsPerXUnit = 800 / mapWidth;

    final double pixelsPerYUnit = -800 / mapHeight;

    final double originX = boundingBox.getMinX() - 2;
    final double originY = boundingBox.getMaxY() + 2;

    transform.concatenate(AffineTransform.getScaleInstance(pixelsPerXUnit, pixelsPerYUnit));
    transform.concatenate(AffineTransform.getTranslateInstance(-originX, -originY));
    final ValueHolder<List<Polygon>> polygons = new ValueHolder<>(Collections.emptyList());
    final ValueHolder<Point> pointHolder = new ValueHolder<>(null);
    // Invoke.later(() -> {
    // final JFrame frame = new JFrame();
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setSize(850, 850);
    // frame.setVisible(true);
    // frame.setLayout(new BorderLayout());
    // frame.add(new BasePanel() {
    //
    // @Override
    // public void paint(final Graphics graphics) {
    // final Graphics2D g2 = (Graphics2D)graphics;
    // g2.setPaint(WebColors.White);
    // g2.fillRect(0, 0, 800, 800);
    // final AffineTransform oldTransform = g2.getTransform();
    // g2.transform(transform);
    // synchronized (tin) {
    // g2.setStroke(new BasicStroke((float)(1 / pixelsPerXUnit)));
    // for (final Polygon polygon : polygons.getValue()) {
    // g2.setPaint(WebColors.newAlpha(WebColors.Aqua, 25));
    // g2.fill(polygon);
    // g2.setColor(WebColors.Black);
    // g2.draw(polygon);
    // }
    // for (int vertexIndex = 0; vertexIndex < tin.getVertexCount(); vertexIndex++) {
    // final Point point = tin.getVertex(vertexIndex);
    // final double x = point.getX();
    // final double y = point.getY();
    // final double size = 7;
    // final double half = size / 2;
    // final Ellipse2D.Double shape = new Ellipse2D.Double(x - half / pixelsPerXUnit,
    // y - half / pixelsPerXUnit, size / pixelsPerXUnit, size / pixelsPerXUnit);
    // g2.setPaint(WebColors.Yellow);
    // g2.fill(shape);
    // g2.setColor(WebColors.Black);
    // g2.draw(shape);
    // }
    // {
    // final Point point = pointHolder.getValue();
    // if (point != null) {
    // g2.setPaint(WebColors.Red);
    // final double x = point.getX();
    // final double y = point.getY();
    // final double size = 9;
    // final double half = size / 2;
    // g2.fill(new Ellipse2D.Double(x - half / pixelsPerXUnit, y - half / pixelsPerXUnit,
    // size / pixelsPerXUnit, size / pixelsPerXUnit));
    // }
    // }
    // g2.setTransform(oldTransform);
    // g2.translate(0, 800);
    // g2.setPaint(WebColors.Green);
    // for (int vertexIndex = 0; vertexIndex < tin.getVertexCount(); vertexIndex++) {
    // final Point point = tin.getVertex(vertexIndex);
    // final double x = point.getX();
    // final double y = point.getY();
    // final int screenX = (int)((x + 2) * pixelsPerXUnit);
    // final int screenY = (int)((y + 2) * pixelsPerYUnit);
    // g2.drawString(Integer.toString(vertexIndex), screenX + 5, screenY);
    // }
    // }
    // }
    // }, BorderLayout.CENTER);
    // Invoke.background("tin", () -> {
    // tin.newTriangulatedIrregularNetwork((x, y, triangles) -> {
    // final List<Polygon> polygons2 = new ArrayList<>();
    // for (final Triangle t : triangles) {
    // System.out.println(t);
    // polygons2.add(geometryFactory.polygon(3, t.getRing(0).getCoordinates()));
    // }
    // System.out.println(polygons2.size());
    // polygons.setValue(polygons2);
    // pointHolder.setValue(GeometryFactory.DEFAULT.point(x, y));
    // Invoke.later(frame::repaint);
    // });
    // });
    // });

  }

  public static void tinVisualLas() {
    final LasReader reader = new LasReader();
    final LasPointCloud pointCloud = (LasPointCloud)reader
      .readPointCloud(new PathResource("/Users/paustin/Downloads/points.las"));

    final List<Point> points = new ArrayList<>();
    final List<LasPoint0Core> lasPoints = pointCloud.getPoints();
    for (int i = 0; i < 4; i++) {
      points.add(lasPoints.get(i));
    }

    final GeometryFactory geometryFactory = pointCloud.getGeometryFactory();
    tinVisual(geometryFactory, points);

  }
}
