package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.Property;

public class SelectMapCoordinateSystem extends ComboBox implements
  ItemListener, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final Reference<MapPanel> map;

  public SelectMapCoordinateSystem(final MapPanel map) {
    super(3857, 3005// , 26907, 26908, 26909, 26910, 26911
    );

    this.map = new WeakReference<MapPanel>(map);
    setSelectedItem(map.getGeometryFactory().getSrid());
    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(
      this, "formatCoordinateSystem");
    setRenderer(renderer);
    AutoCompleteDecorator.decorate(this, renderer);
    addItemListener(this);
    Property.addListener(map, "geometryFactory", this);
    final Dimension size = new Dimension(200, 22);
    setMaximumSize(size);
    setToolTipText("Coordinate System");
  }

  public String formatCoordinateSystem(final Object value) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
    if (coordinateSystem == null) {
      return StringConverterRegistry.toString(value);
    } else {
      return coordinateSystem.getId() + " " + coordinateSystem.getName();
    }
  }

  public CoordinateSystem getCoordinateSystem(final Object value) {
    CoordinateSystem coordinateSystem = null;
    if (value instanceof CoordinateSystem) {
      coordinateSystem = (CoordinateSystem)value;
    } else if (value != null) {
      try {
        final int coordinateSystemId = Integer.parseInt(StringConverterRegistry.toString(value));
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      } catch (final Throwable t) {
      }
    }
    return coordinateSystem;
  }

  public MapPanel getMap() {
    return map.get();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    final MapPanel map = getMap();
    if (map != null) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final Object value = e.getItem();
        final CoordinateSystem coordinateSystem = getCoordinateSystem(value);
        if (coordinateSystem != null) {
          map.setGeometryFactory(GeometryFactory.getFactory(coordinateSystem));
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
        final com.revolsys.jts.geom.GeometryFactory geometryFactory = map.getGeometryFactory();
        final int srid = geometryFactory.getSrid();
        setSelectedItem(srid);
      }
    }
  }

}
