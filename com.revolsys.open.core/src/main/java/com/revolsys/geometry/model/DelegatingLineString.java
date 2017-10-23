package com.revolsys.geometry.model;

import java.util.function.Consumer;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.util.function.BiConsumerDouble;
import com.revolsys.util.function.BiFunctionDouble;
import com.revolsys.util.function.Function4Double;

public interface DelegatingLineString extends LineString {
  @Override
  default boolean equalsVertex2d(final int vertexIndex, final double x, final double y) {
    final LineString line = getLineString();
    return line.equalsVertex2d(vertexIndex, x, y);
  }

  @Override
  default boolean equalsVertex2d(final int vertexIndex1, final int vertexIndex2) {
    final LineString line = getLineString();
    return line.equalsVertex2d(vertexIndex1, vertexIndex2);
  }

  @Override
  default <R> R findSegment(final Function4Double<R> action) {
    final LineString line = getLineString();
    return line.findSegment(action);
  }

  @Override
  default <R> R findVertex(final BiFunctionDouble<R> action) {
    final LineString line = getLineString();
    return line.findVertex(action);
  }

  @Override
  default void forEachVertex(final BiConsumerDouble action) {
    final LineString line = getLineString();
    line.forEachVertex(action);
  }

  @Override
  default void forEachVertex(final Consumer<double[]> action) {
    final LineString line = getLineString();
    line.forEachVertex(action);
  }

  @Override
  default void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final double[] coordinates, final Consumer<double[]> action) {
    final LineString line = getLineString();
    line.forEachVertex(coordinatesOperation, coordinates, action);
  }

  @Override
  default void forEachVertex(final GeometryFactory geometryFactory,
    final Consumer<double[]> action) {
    final LineString line = getLineString();
    line.forEachVertex(geometryFactory, action);
  }

  @Override
  default int getAxisCount() {
    final LineString line = getLineString();
    return line.getAxisCount();
  }

  @Override
  default BoundingBox getBoundingBox() {
    final LineString line = getLineString();
    return line.getBoundingBox();
  }

  @Override
  default double getCoordinate(final int vertexIndex, final int axisIndex) {
    final LineString line = getLineString();
    return line.getCoordinate(vertexIndex, axisIndex);
  }

  @Override
  default double[] getCoordinates() {
    final LineString line = getLineString();
    return line.getCoordinates();
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final LineString line = getLineString();
    return line.getGeometryFactory();
  }

  @Override
  default double getLength() {
    final LineString line = getLineString();
    return line.getLength();
  }

  LineString getLineString();

  @Override
  default double getM(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getM(vertexIndex);
  }

  @Override
  default Point getPoint() {
    final LineString line = getLineString();
    return line.getPoint();
  }

  @Override
  default Point getPoint(final int i) {
    final LineString line = getLineString();
    return line.getPoint(i);
  }

  @Override
  default int getVertexCount() {
    final LineString line = getLineString();
    return line.getVertexCount();
  }

  @Override
  default double getX(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getX(vertexIndex);
  }

  @Override
  default double getY(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getY(vertexIndex);
  }

  @Override
  default double getZ(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getZ(vertexIndex);
  }

  @Override
  default String toWkt() {
    final LineString line = getLineString();
    return line.toWkt();
  }
}
