package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComboBox;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.MapPanel;

@SuppressWarnings("serial")
public class SelectMapScale extends JComboBox implements ItemListener,
  PropertyChangeListener, ActionListener {
  private static final Object[] SCALES = {
    500000000.0, 250000000.0, 100000000.0, 50000000.0, 25000000.0, 10000000.0,
    5000000.0, 2500000.0, 1000000.0, 500000.0, 250000.0, 125000.0, 50000.0,
    20000.0, 10000.0, 5000.0, 2500.0, 2000.0, 1000.0
  };

  private final MapPanel map;

  public SelectMapScale(final MapPanel map) {
    super(SCALES);
    this.map = map;
    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(
      MapScale.class, "formatScale");
    final SelectMapScaleEditor editor = new SelectMapScaleEditor(getEditor(),
      renderer);
    setEditor(editor);
    setRenderer(renderer);
    addItemListener(this);
    addActionListener(this);
    map.addPropertyChangeListener("scale", this);
    final Dimension size = new Dimension(140, 30);
    setPreferredSize(size);
    setMaximumSize(size);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      final Object item = getSelectedItem();
      String string = StringConverterRegistry.toString(item);
      string = string.replaceAll("((^1:)|([^0-9\\.])+)", "");
      final double scale = Double.parseDouble(string);
      map.setScale(scale);
    } catch (final Throwable t) {
    }
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
