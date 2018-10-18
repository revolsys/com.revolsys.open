package com.revolsys.elevation.cloud;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.IoFactory;
import com.revolsys.predicate.Predicates;
import com.revolsys.spring.resource.Resource;

public interface PointCloud<P extends Point>
  extends BaseCloseable, GeometryFactoryProxy, BoundingBoxProxy {

  static <P extends Point, PC extends PointCloud<P>> PC newPointCloud(final Object source) {
    return newPointCloud(source, MapEx.EMPTY);
  }

  static <P extends Point, PC extends PointCloud<P>> PC newPointCloud(final Object source,
    final GeometryFactory defaultGeometryFactory) {
    final MapEx properties = new LinkedHashMapEx("geometryFactory", defaultGeometryFactory);
    return newPointCloud(source, properties);
  }

  @SuppressWarnings("unchecked")
  static <P extends Point, PC extends PointCloud<P>> PC newPointCloud(final Object source,
    final MapEx properties) {
    final PointCloudReadFactory factory = IoFactory.factory(PointCloudReadFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(source);
      return (PC)factory.newPointCloud(resource, properties);
    }
  }

  void forEachPoint(final Consumer<? super P> action);

  default Predicate<Point> getDefaultFilter() {
    return Predicates.all();
  }

  default GriddedElevationModel newGriddedElevationModel(
    final Map<String, ? extends Object> properties) {
    final TriangulatedIrregularNetwork tin = newTriangulatedIrregularNetwork();
    final BoundingBox boundingBox = getBoundingBox();
    final int minX = (int)Math.floor(boundingBox.getMinX());
    final int minY = (int)Math.floor(boundingBox.getMinY());
    final int maxX = (int)Math.ceil(boundingBox.getMaxX());
    final int maxY = (int)Math.ceil(boundingBox.getMaxY());
    final int width = maxX - minX;
    final int height = maxY - minY;
    final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
      getGeometryFactory().convertAxisCountAndScales(3, 1000.0, 1000.0, 1000.0), minX, minY, width,
      height, 1);

    tin.forEachTriangle(elevationModel::setElevationsForTriangle);
    return null;
  }

  default TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    final Predicate<Point> filter = getDefaultFilter();
    return newTriangulatedIrregularNetwork(filter);
  }

  default TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(
    final Predicate<? super Point> filter) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    forEachPoint((point) -> {
      if (filter.test(point)) {
        tinBuilder.insertVertex(point);
      }
    });
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork();
    return tin;
  }

  void refreshClassificationCounts();

  String toHtml();
}
