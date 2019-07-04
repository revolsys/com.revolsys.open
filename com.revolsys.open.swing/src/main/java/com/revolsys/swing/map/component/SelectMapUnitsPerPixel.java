package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.field.FunctionStringConverter;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class SelectMapUnitsPerPixel extends JComboBox<Double>
  implements ItemListener, PropertyChangeListener, ActionListener {

  private static final DecimalFormatSymbols FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance();

  static {
    FORMAT_SYMBOLS.setGroupingSeparator(' ');
  }

  private static final long serialVersionUID = 1L;

  private static DefaultComboBoxModel<Double> newModel(final List<Double> unitsPerPixelList) {
    return new DefaultComboBoxModel<>(new Vector<>(unitsPerPixelList));
  }

  private static DefaultComboBoxModel<Double> newModel(final Viewport2D viewport) {
    final List<Double> unitsPerPixelList = viewport.getUnitsPerPixelList();
    return newModel(unitsPerPixelList);
  }

  private final Viewport2D viewport;

  private String unitLabel = "m";

  private NumberFormat format;

  private boolean projected;

  private List<Double> unitsPerPixelList;

  public SelectMapUnitsPerPixel(final MapPanel map) {
    super(newModel(map.getViewport()));
    final Viewport2D viewport = map.getViewport();
    this.unitsPerPixelList = viewport.getUnitsPerPixelList();

    this.format = new DecimalFormat("#,###.###", FORMAT_SYMBOLS);

    this.viewport = map.getViewport();

    setEditable(true);
    final FunctionStringConverter<Double> renderer = new FunctionStringConverter<>(this::format);
    renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    final SelectMapScaleEditor editor = new SelectMapScaleEditor(getEditor(), renderer);
    setEditor(editor);
    setRenderer(renderer);
    addItemListener(this);
    addActionListener(this);
    Property.addListener(this.viewport, this);
    final Dimension size = new Dimension(120, 22);
    setPreferredSize(size);
    setMaximumSize(size);
    setToolTipText("Resolution (m/pixel)");
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      final Object item = getSelectedItem();
      double unitsPerPixel;
      if (item instanceof Double) {
        unitsPerPixel = (Double)item;

      } else {
        String string = DataTypes.toString(item);
        string = string.replaceAll("([^0-9\\.])+", "");
        unitsPerPixel = Double.parseDouble(string);
      }
      this.viewport.setUnitsPerPixel(unitsPerPixel);
    } catch (final Throwable t) {
    }
  }

  private String format(final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      final double doubleValue = number.doubleValue();
      return this.format.format(doubleValue) + this.unitLabel;
    } else {
      return "Unknown";
    }
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      double unitsPerPixel = this.viewport.getUnitsPerPixel();
      final Object value = e.getItem();
      if (value instanceof Double) {
        unitsPerPixel = (Double)value;
      }
      this.viewport.setUnitsPerPixel(unitsPerPixel);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("unitsPerPixel".equals(propertyName)) {
      final double unitsPerPixel = this.viewport.getUnitsPerPixel();
      if (unitsPerPixel > 0 && Double.isFinite(unitsPerPixel)) {
        Invoke.later(() -> setSelectedItem(unitsPerPixel));
      }
    } else if ("geometryFactory".equals(propertyName)) {
      String toolTip;
      final GeometryFactory geometryFactory = this.viewport.getGeometryFactory();
      if (geometryFactory == null) {
        toolTip = "Map Resolution (m/pixel)";
      } else {
        final HorizontalCoordinateSystem coordinateSystem = geometryFactory
          .getHorizontalCoordinateSystem();
        this.projected = !geometryFactory.isGeographic();
        if (this.projected) {
          this.format = new DecimalFormat("#,###.###", FORMAT_SYMBOLS);

        } else {
          this.format = new DecimalFormat("#,###.#######", FORMAT_SYMBOLS);
        }
        this.unitLabel = coordinateSystem.getUnitLabel();
        toolTip = "Map Resolution (" + this.unitLabel + "/pixel)";
      }

      Invoke.later(() -> {
        setToolTipText(toolTip);
        final List<Double> unitsPerPixelList = this.viewport.getUnitsPerPixelList();

        if (!unitsPerPixelList.equals(unitsPerPixelList)) {
          this.unitsPerPixelList = unitsPerPixelList;
          final ComboBoxModel<Double> model = newModel(unitsPerPixelList);
          setModel(model);
          final double unitsPerPixel = this.viewport.getUnitsPerPixel();
          setSelectedItem(unitsPerPixel);
        }
      });
    }
  }

}
