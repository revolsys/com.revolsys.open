package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.epsg.EpsgId;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.field.ArrayListComboBoxModel;
import com.revolsys.swing.field.BaseComboBox;

public class CoordinateSystemField extends BaseComboBox<Integer> implements ItemListener {
  private static final long serialVersionUID = 1L;

  public static String formatCoordinateSystem(final Integer value) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
    if (coordinateSystem == null) {
      return DataTypes.toString(value);
    } else {
      return coordinateSystem.getHorizontalCoordinateSystemId() + " "
        + coordinateSystem.getCoordinateSystemName();
    }
  }

  public static String formatCoordinateSystem(final Object object) {
    if (object instanceof CoordinateSystem) {
      final CoordinateSystem coordinateSystem = (CoordinateSystem)object;
      final int id = coordinateSystem.getCoordinateSystemId();
      final String name = coordinateSystem.getCoordinateSystemName();
      if (id > 0) {
        return id + " " + name;
      } else {
        return name;
      }
    } else {
      return "-";
    }
  }

  public static CoordinateSystem getCoordinateSystem(final Object value) {
    CoordinateSystem coordinateSystem = null;
    if (value instanceof CoordinateSystem) {
      coordinateSystem = (CoordinateSystem)value;
    } else if (value != null) {
      try {
        final int coordinateSystemId = Integer.parseInt(DataTypes.toString(value));
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      } catch (final Throwable t) {
      }
    }
    return coordinateSystem;
  }

  private static ArrayListComboBoxModel<Integer> newModel() {
    return new ArrayListComboBoxModel<>(3857, 3005, //
      EpsgId.nad83Utm(7), //
      EpsgId.nad83Utm(8), //
      EpsgId.nad83Utm(9), //
      EpsgId.nad83Utm(10), //
      EpsgId.nad83Utm(11), //
      EpsgId.WGS84, EpsgId.NAD83, EpsgId.NAD27);
  }

  public CoordinateSystemField(final String fieldName) {
    super("fieldName", newModel(), CoordinateSystemField::formatCoordinateSystem);

    setEditable(false);
    addItemListener(this);
    final Dimension size = new Dimension(240, 22);
    setMaximumSize(size);
    setToolTipText("Coordinate System");
  }

  public void addCoordinateSystem(final CoordinateSystem coordinateSystem) {
    final int srid = coordinateSystem.getHorizontalCoordinateSystemId();
    addCoordinateSystem(srid);
  }

  public void addCoordinateSystem(final int srid) {
    final ArrayListComboBoxModel<Integer> model = (ArrayListComboBoxModel<Integer>)getModel();
    if (model.indexOf(srid) == -1) {
      model.addElement(srid);
    }
  }

  protected void geometryFactoryChanged(final GeometryFactory geometryFactory) {
  }

  public GeometryFactory getGeometryFactory() {
    final Integer coordinateSystemId = getSelectedItem();
    return GeometryFactory.floating2d(coordinateSystemId);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final int stateChange = e.getStateChange();
    if (stateChange == ItemEvent.SELECTED) {
      final Object value = e.getItem();
      final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
      if (coordinateSystem != null) {
        final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactory();
        geometryFactoryChanged(geometryFactory);
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    final int srid = geometryFactory.getHorizontalCoordinateSystemId();
    setSelectedItem(srid);
  }
}
