package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.DefaultComboBoxModel;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.Property;

public class SelectMapCoordinateSystem extends ComboBox implements ItemListener,
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final Reference<MapPanel> map;

  @SuppressWarnings({
    "unchecked"
  })
  public SelectMapCoordinateSystem(final MapPanel map) {
    super(3857, 3005, 26907, 26908, 26909, 26910, 26911, 4326, 4269, 4267);

    this.map = new WeakReference<MapPanel>(map);
    setSelectedItem(map.getGeometryFactory().getSrid());
    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(this,
      "formatCoordinateSystem");
    setRenderer(renderer);
    AutoCompleteDecorator.decorate(this, renderer);
    addItemListener(this);
    Property.addListener(map, "geometryFactory", this);
    final Dimension size = new Dimension(200, 22);
    setMaximumSize(size);
    setToolTipText("Coordinate System");
  }

  public void addCoordinateSystem(final CoordinateSystem coordinateSystem) {
    final int srid = coordinateSystem.getId();
    addCoordinateSystem(srid);
  }

  @SuppressWarnings("unchecked")
  public void addCoordinateSystem(final int srid) {
    final DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>)getModel();
    if (model.getIndexOf(srid) == -1) {
      model.addElement(srid);
    }
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
        final int srid = geometryFactory.getSrid();
        setSelectedItem(srid);
      }
    }
  }

}
