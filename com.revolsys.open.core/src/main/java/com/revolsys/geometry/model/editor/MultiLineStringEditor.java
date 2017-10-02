package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiLineString;

public class MultiLineStringEditor
  extends AbstractGeometryCollectionEditor<Lineal, LineString, LineStringEditor>
  implements MultiLineString, LinealEditor {
  private static final long serialVersionUID = 1L;

  private Lineal lineal;

  public MultiLineStringEditor(final GeometryCollectionImplEditor geometryEditor,
    final Lineal lineal) {
    super(geometryEditor, lineal);
    this.lineal = lineal;
  }

  public MultiLineStringEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public MultiLineStringEditor(final Lineal lineal) {
    this(null, lineal);
  }

  @Override
  public MultiLineStringEditor clone() {
    return (MultiLineStringEditor)super.clone();
  }

  @Override
  public Lineal newLineal(final GeometryFactory geometryFactory, final LineString... lines) {
    return this.lineal.newLineal(geometryFactory, lines);
  }

  @Override
  public double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    final LineStringEditor editor = getEditor(partIndex);
    if (editor == null) {
      return Double.NaN;
    } else {
      return editor.setCoordinate(vertexIndex, axisIndex, coordinate);
    }
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
