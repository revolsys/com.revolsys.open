package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class SelectMapScale extends JComboBox
  implements ItemListener, PropertyChangeListener, ActionListener {
  private static final long serialVersionUID = 1L;

  private final Reference<MapPanel> map;

  public SelectMapScale(final MapPanel map) {
    super(new Vector<Long>(map.getScales()));
    this.map = new WeakReference<MapPanel>(map);

    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(MapScale.class,
      "formatScale");
    renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    final SelectMapScaleEditor editor = new SelectMapScaleEditor(getEditor(), renderer);
    setEditor(editor);
    setRenderer(renderer);
    addItemListener(this);
    addActionListener(this);
    Property.addListener(map, "scale", this);
    final Dimension size = new Dimension(140, 22);
    setPreferredSize(size);
    setMaximumSize(size);
    setToolTipText("Map Scale");
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final MapPanel map = getMap();
    if (map != null) {
      try {
        final Object item = getSelectedItem();
        String string = StringConverterRegistry.toString(item);
        string = string.replaceAll("((^1:)|([^0-9\\.])+)", "");
        final double scale = Double.parseDouble(string);
        map.setScale(scale);
      } catch (final Throwable t) {
      }
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
        double scale = map.getScale();
        final Object value = e.getItem();
        if (value instanceof Double) {
          scale = (Double)value;
        }
        map.setScale(scale);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final MapPanel map = getMap();
    if (map != null) {
      final String propertyName = event.getPropertyName();

      if ("scale".equals(propertyName)) {
        final double scale = map.getScale();
        double currentScale = 0;
        final Object currentValue = getSelectedItem();
        if (currentValue instanceof Number) {
          currentScale = ((Number)currentValue).doubleValue();
        } else if (Property.hasValue(currentValue)) {
          final String scaleString = currentValue.toString()
            .replaceAll("1:", "")
            .replaceAll("[^0-9\\.]+", "");
          if (Property.hasValue(scaleString)) {
            try {
              currentScale = Double.valueOf(scaleString);
            } catch (final Throwable t) {
            }
          }
        }
        final Number newValue = (Number)event.getNewValue();

        if (scale > 0 && !Double.isInfinite(scale) && !Double.isNaN(scale)) {
          if (currentScale != newValue.doubleValue()) {
            Invoke.later(this, "setSelectedItem", scale);
          }
        }
      }
    }
  }

}
