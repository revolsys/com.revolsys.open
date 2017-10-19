package com.revolsys.geometry.model.editor;

import java.util.function.Consumer;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.util.function.BiConsumerDouble;
import com.revolsys.util.function.BiFunctionDouble;
import com.revolsys.util.function.Function4Double;

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
  public <R> R findSegment(final Function4Double<R> action) {
    for (final Geometry geometry : this.editors) {
      final R result = geometry.findSegment(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    for (final GeometryEditor<?> editor : this.editors) {
      final R result = editor.findVertex(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    for (final GeometryEditor<?> editor : this.editors) {
      action.accept(editor);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final GeometryEditor<?> editor : this.editors) {
      editor.forEachVertex(action);
    }
  }

  @Override
  public int getLineStringCount() {
    return this.editors.size();
  }

  @Override
  public GeometryDataType<LineString, LineStringEditor> getPartDataType() {
    return DataTypes.LINE_STRING;
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
