package com.revolsys.geometry.model.editor;

import java.util.function.Consumer;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.util.function.BiConsumerDouble;

public class MultiPolygonEditor
  extends AbstractGeometryCollectionEditor<Polygonal, Polygon, PolygonEditor>
  implements MultiPolygon, PolygonalEditor {
  private static final long serialVersionUID = 1L;

  private Polygonal polygonal;

  public MultiPolygonEditor(final GeometryCollectionImplEditor parentEditor,
    final Polygonal polygonal) {
    super(parentEditor, polygonal);
    this.polygonal = polygonal;
  }

  public MultiPolygonEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public MultiPolygonEditor(final Polygonal polygonal) {
    this(null, polygonal);
  }

  @Override
  public MultiPolygonEditor clone() {
    return (MultiPolygonEditor)super.clone();
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    for (final GeometryEditor<?> editor : this.editors) {
      action.accept(editor);
    }
  }

  @Override
  public void forEachPolygon(final Consumer<Polygon> action) {
    for (int i = 0; i < getGeometryCount(); i++) {
      final Polygon polygon = getEditor(i);
      action.accept(polygon);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final GeometryEditor<?> editor : this.editors) {
      editor.forEachVertex(action);
    }
  }

  @Override
  public GeometryDataType<Polygon, PolygonEditor> getPartDataType() {
    return DataTypes.POLYGON;
  }

  @Override
  public Polygonal newPolygonal(final GeometryFactory geometryFactory, final Polygon... polygons) {
    return this.polygonal.newPolygonal(geometryFactory, polygons);
  }

  @Override
  public double setCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex, final double coordinate) {
    final PolygonEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(ringIndex, vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
