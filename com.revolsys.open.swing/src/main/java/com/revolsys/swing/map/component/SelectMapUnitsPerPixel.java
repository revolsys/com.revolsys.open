package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Vector;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.map.MapPanel;

public class SelectMapUnitsPerPixel extends JComboBox implements ItemListener,
  PropertyChangeListener, ActionListener {
  private static ComboBoxModel GEOGRAPHIC_MODEL = new DefaultComboBoxModel(
    new Vector<Double>(Arrays.asList(2.0, 1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01,
      0.005, 0.002, 0.001, 0.0005, 0.0002, 0.0001, 0.00005, 0.00002, 0.00001,
      0.000005, 0.000002, 0.000001, 0.0000005, 0.0000002, 0.0000001)));

  private static ComboBoxModel PROJECTED_MODEL = new DefaultComboBoxModel(
    new Vector<Double>(Arrays.asList(500000.0, 200000.0, 100000.0, 50000.0,
      20000.0, 10000.0, 5000.0, 2000.0, 1000.0, 500.0, 200.0, 100.0, 50.0,
      20.0, 10.0, 5.0, 2.0, 1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002,
      0.001)));

  private static final long serialVersionUID = 1L;

  private final MapPanel map;

  private String unitString;

  public SelectMapUnitsPerPixel(final MapPanel map) {
    super(PROJECTED_MODEL);
    this.map = map;

    setEditable(true);
    final InvokeMethodStringConverter renderer = new InvokeMethodStringConverter(
      this, "format");
    renderer.setHorizontalAlignment(JLabel.RIGHT);
    final SelectMapScaleEditor editor = new SelectMapScaleEditor(getEditor(),
      renderer);
    setEditor(editor);
    setRenderer(renderer);
    addItemListener(this);
    addActionListener(this);
    map.addPropertyChangeListener(this);
    final Dimension size = new Dimension(120, 22);
    setPreferredSize(size);
    setMaximumSize(size);
    setToolTipText("m/pixel");
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      final Object item = getSelectedItem();
      String string = StringConverterRegistry.toString(item);
      string = string.replaceAll("([^0-9\\.])+", "");
      final double unitsPerPixel = Double.parseDouble(string);
      this.map.setUnitsPerPixel(unitsPerPixel);
    } catch (final Throwable t) {
    }
  }

  public String format(final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      int numDigits;
      if (getModel() == PROJECTED_MODEL) {
        numDigits = 3;
      } else {
        numDigits = 7;
      }
      return new BigDecimal(number.doubleValue()).setScale(numDigits,
        BigDecimal.ROUND_HALF_UP).toPlainString()
        + unitString;
    } else {
      return "Unknown";
    }
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      double unitsPerPixel = this.map.getUnitsPerPixel();
      final Object value = e.getItem();
      if (value instanceof Double) {
        unitsPerPixel = (Double)value;
      }
      this.map.setUnitsPerPixel(unitsPerPixel);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("scale".equals(propertyName) || "unitsPerPixel".equals(propertyName)) {
      final double unitsPerPixel = this.map.getUnitsPerPixel();
      setSelectedItem(unitsPerPixel);
    } else if ("boundingBox".equals(propertyName)) {
      final BoundingBox boundingBox = this.map.getBoundingBox();
      ComboBoxModel model = PROJECTED_MODEL;
      if (boundingBox == null) {
        setToolTipText("m/pixel");
      } else {
        final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
        final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
        if (coordinateSystem instanceof GeographicCoordinateSystem) {
          model = GEOGRAPHIC_MODEL;
        } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
          model = GEOGRAPHIC_MODEL;
        }
        final Unit<Quantity> unit = coordinateSystem.getUnit();
        this.unitString = unit.toString();
        setToolTipText(unit + "/pixel");
      }
      if (model != getModel()) {
        setModel(model);
      }

    }
  }

}
