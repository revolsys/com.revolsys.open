package com.revolsys.elevation.tin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.index.BoundingBoxSpatialIndex;
import com.revolsys.geometry.index.rtree.RTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.spring.resource.Resource;

public class TriangulatedIrregularNetworkImpl implements TriangulatedIrregularNetwork {

  private final BoundingBox boundingBox;

  private final GeometryFactory geometryFactory;

  private RTree<Triangle> triangleIndex;

  private Resource resource;

  private final Set<Point> nodes = new LinkedHashSet<>();

  private List<Triangle> triangles = new ArrayList<>();

  public TriangulatedIrregularNetworkImpl(final BoundingBox boundingBox,
    final Iterable<? extends Triangle> triangles) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.triangles = Lists.toArray(triangles);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public Set<Point> getNodes() {
    return this.nodes;
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public int getSize() {
    return this.triangles.size();
  }

  @Override
  public BoundingBoxSpatialIndex<Triangle> getTriangleIndex() {
    if (this.triangleIndex == null) {
      this.triangleIndex = new RTree<>();
      for (final Triangle triangle : this.triangles) {
        for (int i = 0; i < 3; i++) {
          this.nodes.add(triangle.getPoint(i));
        }
        this.triangleIndex.put(triangle.getBoundingBox(), triangle);
      }
    }
    return this.triangleIndex;
  }

  @Override
  public List<Triangle> getTriangles() {
    return this.triangles;
  }

}
