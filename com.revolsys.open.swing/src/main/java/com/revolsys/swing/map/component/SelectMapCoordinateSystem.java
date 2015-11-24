package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.field.ArrayListComboBoxModel;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.Property;

public class SelectMapCoordinateSystem extends ComboBox<Integer>
  implements ItemListener, PropertyChangeListener {
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

  private final Reference<MapPanel> map;

  public SelectMapCoordinateSystem(final MapPanel map) {
    super("srid", new ArrayListComboBoxModel<Integer>(3857, 3005, 26907, 26908, 26909, 26910, 26911,
      4326, 4269, 4267), SelectMapCoordinateSystem::formatCoordinateSystem, null);

    this.map = new WeakReference<MapPanel>(map);
    setSelectedItem(map.getGeometryFactory().getCoordinateSystemId());
    setEditable(true);
    addItemListener(this);
    Property.addListener(map, "geometryFactory", this);
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

  public MapPanel getMap() {
    return this.map.get();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final MapPanel map = getMap();
    if (map != null) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final Object value = e.getItem();
        final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
        if (coordinateSystem != null) {
          map.setGeometryFactory(coordinateSystem.getGeometryFactory());
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final MapPanel map = getMap();
    if (map != null) {
      final String propertyName = event.getPropertyName();
      if ("geometryFactory".equals(propertyName)) {
        final GeometryFactory geometryFactory = map.getGeometryFactory();
        final int srid = geometryFactory.getCoordinateSystemId();
        setSelectedItem(srid);
      }
    }
  }

}
