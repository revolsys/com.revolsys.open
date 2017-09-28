package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;

public class MultiPointEditor extends AbstractGeometryCollectionEditor<Punctual, Point, PointEditor>
  implements MultiPoint, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private Punctual punctual;

  public MultiPointEditor(final AbstractGeometryEditor<?> parentEditor, final Punctual punctual) {
    super(parentEditor, punctual);
    this.punctual = punctual;
  }

  public MultiPointEditor(final GeometryCollectionImplEditor parentEditor,
    final GeometryFactory geometryFactory, final PointEditor... editors) {
    super(parentEditor, geometryFactory, editors);
  }

  public MultiPointEditor(final Punctual punctual) {
    this(null, punctual);
  }

  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId, final Point point) {
    if (geometryId == null || geometryId.length != 0) {
    } else {
      appendVertex(point);
    }
    return this;
  }

  public void appendVertex(final Point point) {
    if (point != null && !point.isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point newPoint = point.convertGeometry(geometryFactory);
      final PointEditor pointEditor = newPoint.newGeometryEditor(this);
      addEditor(pointEditor);
    }
  }

  @Override
  public MultiPoint clone() {
    return (MultiPoint)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  @Override
  public Punctual newPunctual(final GeometryFactory geometryFactory, final Point... points) {
    if (this.punctual == null) {
      return MultiPoint.super.newPunctual(geometryFactory, points);
    } else {
      return this.punctual.newPunctual(geometryFactory, points);
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    final PointEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
