package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.MapPanel;

@SuppressWarnings("serial")
public class SelectMapScale extends ComboBox implements ItemListener,
  PropertyChangeListener {
  private static final Object[] SCALES = {
    16000000.0, 8000000.0, 4000000.0, 2000000.0, 1000000.0, 500000.0, 250000.0,
    125000.0, 50000.0, 20000.0, 10000.0, 5000.0, 2500.0, 2000.0, 1000.0
  };

  private final MapPanel map;

  public SelectMapScale(final MapPanel map) {
    super(SCALES);
    this.map = map;
    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(
      MapScale.class, "formatScale");
    setRenderer(renderer);
    AutoCompleteDecorator.decorate(this, renderer);
    addItemListener(this);
    map.addPropertyChangeListener("scale", this);
    final Dimension size = new Dimension(120, 30);
    setMaximumSize(size);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      double scale = map.getScale();
      final Object value = e.getItem();
      if (value instanceof Double) {
        scale = (Double)value;
      }
      map.setScale(scale);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("scale".equals(propertyName)) {
      final double scale = map.getScale();
      setSelectedItem(scale);
    }
  }

  public void setScales(final List<Double> resolutionList) {
    // TODO Auto-generated method stub

  }
}
