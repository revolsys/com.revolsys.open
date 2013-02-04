package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComboBox;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.Viewport2D;

@SuppressWarnings("serial")
public class SelectMapScale extends JComboBox implements ItemListener,
  PropertyChangeListener {
  private static final Object[] SCALES = {
    16000000.0, 8000000.0, 4000000.0, 2000000.0, 1000000.0, 500000.0, 250000.0,
    125000.0, 50000.0, 20000.0, 10000.0, 5000.0, 2500.0, 2000.0, 1000.0
  };

  private final Viewport2D viewport;

  public SelectMapScale(final Viewport2D viewport) {
    super(SCALES);
    this.viewport = viewport;
    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(
      MapScale.class, "formatScale");
    setRenderer(renderer);
    AutoCompleteDecorator.decorate(this, renderer);
    addItemListener(this);
    viewport.addPropertyChangeListener("scale", this);
    final Dimension size = new Dimension(100, 30);
    setMaximumSize(size);
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      double scale = viewport.getScale();
      final Object value = e.getItem();
      if (value instanceof Double) {
        scale = (Double)value;
      }
      if (viewport.getScale() != scale) {
        viewport.setScale(scale);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("scale".equals(propertyName)) {
      final double scale = viewport.getScale();
      setSelectedItem(scale);
    }
  }

  public void setScales(final List<Double> resolutionList) {
    // TODO Auto-generated method stub

  }
}
