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
import java.util.Arrays;
import java.util.Vector;

import javax.measure.Unit;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.field.FunctionStringConverter;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class SelectMapUnitsPerPixel extends JComboBox
  implements ItemListener, PropertyChangeListener, ActionListener {
  private static ComboBoxModel GEOGRAPHIC_MODEL = new DefaultComboBoxModel(
    new Vector<>(Arrays.asList(2.0, 1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002, 0.001,
      0.0005, 0.0002, 0.0001, 0.00005, 0.00002, 0.00001, 0.000005, 0.000002, 0.000001, 0.0000005,
      0.0000002, 0.0000001)));

  private static ComboBoxModel PROJECTED_MODEL = new DefaultComboBoxModel(
    new Vector<>(Arrays.asList(500000.0, 200000.0, 100000.0, 50000.0, 20000.0, 10000.0, 5000.0,
      2000.0, 1000.0, 500.0, 200.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0, 0.5, 0.2, 0.1, 0.05,
      0.02, 0.01, 0.005, 0.002, 0.001)));

  private static final long serialVersionUID = 1L;

  private final Reference<MapPanel> map;

  private String unitString = "m";

  public SelectMapUnitsPerPixel(final MapPanel map) {
    super(PROJECTED_MODEL);
    this.map = new WeakReference<>(map);

    setEditable(true);
    final FunctionStringConverter renderer = new FunctionStringConverter(this::format);
    renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    final SelectMapScaleEditor editor = new SelectMapScaleEditor(getEditor(), renderer);
    setEditor(editor);
    setRenderer(renderer);
    addItemListener(this);
    addActionListener(this);
    Property.addListener(map, this);
    final Dimension size = new Dimension(120, 22);
    setPreferredSize(size);
    setMaximumSize(size);
    setToolTipText("Resolution (m/pixel)");
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      final Object item = getSelectedItem();
      String string = DataTypes.toString(item);
      string = string.replaceAll("([^0-9\\.])+", "");
      final double unitsPerPixel = Double.parseDouble(string);
      final MapPanel map = getMap();
      if (map != null) {
        map.setUnitsPerPixel(unitsPerPixel);
      }
    } catch (final Throwable t) {
    }
  }

  public String format(final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      double doubleValue = number.doubleValue();
      final ComboBoxModel<?> model = getModel();
      if (model == PROJECTED_MODEL) {
        doubleValue = Doubles.makePrecise(1000, doubleValue);
      } else {
        doubleValue = Doubles.makePrecise(10000000, doubleValue);
      }
      return Doubles.toString(doubleValue) + this.unitString;
    } else {
      return "Unknown";
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
        double unitsPerPixel = map.getUnitsPerPixel();
        final Object value = e.getItem();
        if (value instanceof Double) {
          unitsPerPixel = (Double)value;
        }
        map.setUnitsPerPixel(unitsPerPixel);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final MapPanel map = getMap();
    if (map != null) {

      final String propertyName = event.getPropertyName();
      if ("scale".equals(propertyName) || "unitsPerPixel".equals(propertyName)) {
        final double unitsPerPixel = map.getUnitsPerPixel();
        if (unitsPerPixel > 0 && !Double.isInfinite(unitsPerPixel)
          && !Double.isNaN(unitsPerPixel)) {
          Invoke.later(() -> setSelectedItem(unitsPerPixel));
        }
      } else if ("boundingBox".equals(propertyName)) {
        String toolTip;
        final BoundingBox boundingBox = map.getBoundingBox();
        ComboBoxModel model;
        if (boundingBox == null) {
          toolTip = "Map Resolution (m/pixel)";
          model = PROJECTED_MODEL;
        } else {
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
          if (coordinateSystem instanceof GeographicCoordinateSystem) {
            model = GEOGRAPHIC_MODEL;
          } else {
            model = PROJECTED_MODEL;
          }
          final Unit<?> unit = coordinateSystem.getUnit();
          this.unitString = unit.toString();
          toolTip = "Map Resolution (" + unit + "/pixel)";
        }

      } else if ("geometryFactory".equals(propertyName)) {
        String toolTip;
        final GeometryFactory geometryFactory = map.getGeometryFactory();
        ComboBoxModel model;
        if (geometryFactory == null) {
          toolTip = "Map Resolution (m/pixel)";
          model = PROJECTED_MODEL;
        } else {
          final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
          if (geometryFactory.isGeographic()) {
            model = GEOGRAPHIC_MODEL;
          } else {
            model = PROJECTED_MODEL;
          }
          final Unit<?> unit = coordinateSystem.getUnit();
          this.unitString = unit.toString();
          toolTip = "Map Resolution (" + unit + "/pixel)";
        }
        Invoke.later(() -> {
          setToolTipText(toolTip);
          if (model != getModel()) {
            setModel(model);
          }
        });
      }
    }
  }

}
