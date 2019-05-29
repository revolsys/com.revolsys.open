package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.field.ArrayListComboBoxModel;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.map.ComponentViewport2D;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.Property;

public class SelectMapCoordinateSystem extends ComboBox<Integer> implements ItemListener {
  private static final long serialVersionUID = 1L;

  public static String formatCoordinateSystem(final Object value) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
    if (coordinateSystem == null) {
      return DataTypes.toString(value);
    } else {
      return coordinateSystem.getCoordinateSystemId() + " "
        + coordinateSystem.getCoordinateSystemName();
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

  private ComponentViewport2D viewport;

  private PropertyChangeListener geometryFactoryListener;

  public SelectMapCoordinateSystem(final MapPanel map) {
    super("srid",
      new ArrayListComboBoxModel<>(3857, 3005, 26907, 26908, 26909, 26910, 26911, 4326, 4269, 4267),
      SelectMapCoordinateSystem::formatCoordinateSystem, null);

    this.viewport = map.getViewport();
    final GeometryFactory geometryFactory = this.viewport.getGeometryFactory();
    setSelectedItem(geometryFactory.getCoordinateSystemId());
    setEditable(true);
    addItemListener(this);
    this.geometryFactoryListener = Property.addListenerNewValueSource(this.viewport,
      "geometryFactory", this::setGeometryFactory);
    final Dimension size = new Dimension(200, 22);
    setMaximumSize(size);
    setToolTipText("Coordinate System");
  }

  public void addCoordinateSystem(final CoordinateSystem coordinateSystem) {
    final int srid = coordinateSystem.getCoordinateSystemId();
    addCoordinateSystem(srid);
  }

  public void addCoordinateSystem(final int srid) {
    final ArrayListComboBoxModel<Integer> model = (ArrayListComboBoxModel<Integer>)getModel();
    if (model.indexOf(srid) == -1) {
      model.addElement(srid);
    }
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final int stateChange = e.getStateChange();
    if (stateChange == ItemEvent.SELECTED) {
      final Object value = e.getItem();
      final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
      if (coordinateSystem != null) {
        final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactory();
        this.viewport.setGeometryFactory(geometryFactory);
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Property.removeListener(this.viewport, "geometryFactory", this.geometryFactoryListener);
    this.viewport = null;
    this.geometryFactoryListener = null;
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    final int srid = geometryFactory.getCoordinateSystemId();
    setSelectedItem(srid);
  }
}
