package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SpringLayout;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.component.ValuePanel;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.listener.InvokeMethodActionListener;
import com.revolsys.swing.listener.InvokeMethodChangeListener;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.LineCap;
import com.revolsys.swing.map.layer.dataobject.style.LineJoin;

@SuppressWarnings("serial")
public class LineStylePanel extends ValuePanel<GeometryStyle> implements
  PropertyChangeListener {

  public static GeneralPath getLineShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(19, 19);
    path.lineTo(79, 19);
    path.lineTo(19, 79);
    path.lineTo(79, 79);
    return path;
  }

  public static GeneralPath getPolygonShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(39, 19);
    path.lineTo(59, 19);
    path.lineTo(79, 39);
    path.lineTo(79, 59);
    path.lineTo(59, 79);
    path.lineTo(39, 79);
    path.lineTo(19, 59);
    path.lineTo(19, 39);
    path.closePath();
    return path;
  }

  public static void showDialog(final Component component,
    final GeometryStyle geometryStyle) {
    final LineStylePanel panel = new LineStylePanel(geometryStyle);
    panel.showDialog(component);
  }

  private final GeometryStyle geometryStyle;

  private ColorChooserPanel colorField;

  private TogglePanel lineCapField;

  private TogglePanel lineJoinField;

  private LengthMeasurePanel widthField;

  private DashField dashField;

  public LineStylePanel(final GeometryStyle geometryStyle) {
    super(new BorderLayout());
    this.geometryStyle = geometryStyle;
    setValue(geometryStyle);
    setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(Color.BLACK), "Line"));

    addLineStylePanel();

    final JPanel preview = new JPanel();
    preview.setPreferredSize(new Dimension(100, 100));
    preview.setMinimumSize(new Dimension(100, 100));
    preview.setBackground(Color.WHITE);
    preview.setBorder(BorderFactory.createEtchedBorder());
    add(preview, BorderLayout.EAST);

    setPreferredSize(new Dimension(355, 150));
    setMinimumSize(new Dimension(355, 150));

  }

  private void addLineStylePanel() {
    final JPanel panel = new JPanel(new SpringLayout());

    colorField = new ColorChooserPanel(geometryStyle.getLineColor());
    colorField.addPropertyChangeListener("color", this);
    colorField.setBorder(BorderFactory.createTitledBorder("Line Color"));
    panel.add(colorField);

    final Measure<Length> lineWidth = geometryStyle.getLineWidthMeasure();
    widthField = new LengthMeasurePanel(lineWidth, 0, 300, 50, true,
      lineWidth.getUnit());
    widthField.addPropertyChangeListener("number", this);
    widthField.addPropertyChangeListener("unit", this);
    widthField.setBorder(BorderFactory.createTitledBorder("Line Width"));
    panel.add(widthField);

    // dashField = new DashField(stroke.getDashArray());
    // dashField.addPropertyChangeListener("dash", preview);
    // dashField.setBorder(BorderFactory.createTitledBorder("Line GeometryStyle"));
    // panel.add(dashField);

    final LineJoin lineJoin = geometryStyle.getLineJoinEnum();
    lineJoinField = createLineJoinChooser(lineJoin);
    lineJoinField.addPropertyChangeListener("actionCommand", this);
    lineJoinField.setBorder(BorderFactory.createTitledBorder("Line Join"));
    panel.add(lineJoinField);

    final LineCap lineCap = geometryStyle.getLineCap();
    lineCapField = createLineCapField(lineCap);
    lineCapField.addPropertyChangeListener("actionCommand", this);
    lineCapField.setBorder(BorderFactory.createTitledBorder("Line Cap"));
    panel.add(lineCapField);

    SpringLayoutUtil.makeRows(panel, 0, 0, 5, 5, 2, 2);
    final JScrollPane scrollPane = new JScrollPane(panel);
    add(scrollPane, BorderLayout.CENTER);
  }

  private JComboBox createDashChooserr(final List<Measure<Length>> dash) {
    final JComboBox dashChooser = new JComboBox(new Object[] {

    });

    dashChooser.setSelectedItem(dash);
    return dashChooser;
  }

  private TogglePanel createLineCapField(final LineCap lineCap) {
    final I18nAction lineCapButtAction = new I18nAction("BUTT", null,
      "Butt Cap", SilkIconLoader.getIcon("line_cap_butt"));
    final I18nAction lineCapRoundAction = new I18nAction("ROUND", null,
      "Round Cap", SilkIconLoader.getIcon("line_cap_round"));
    final I18nAction lineCapSquareAction = new I18nAction("SQUARE", null,
      "Square Cap", SilkIconLoader.getIcon("line_cap_square"));
    return new TogglePanel(lineCap.toString(), new Dimension(28, 28),
      lineCapButtAction, lineCapRoundAction, lineCapSquareAction);

  }

  private TogglePanel createLineJoinChooser(final LineJoin lineJoin) {

    final I18nAction miterAction = new I18nAction("MITER", null, "miterJoin",
      SilkIconLoader.getIcon("line_join_miter"));
    final I18nAction roundAction = new I18nAction("ROUND", null, "roundJoin",
      SilkIconLoader.getIcon("line_join_round"));
    final I18nAction bevel = new I18nAction("BEVEL", null, "bevelJoin",
      SilkIconLoader.getIcon("line_join_bevel"));
    return new TogglePanel(lineJoin.toString(), new Dimension(28, 28),
      miterAction, roundAction, bevel);
  }

  private ValuePanel<Measure<Length>> createLineWidthPanel(
    final Measure<Length> width) {
    final ValuePanel<Measure<Length>> panel = new ValuePanel<Measure<Length>>(
      new SpringLayout());
    panel.setValue(width);

    int intValue;
    if (width == null) {
      intValue = 0;
    } else {
      intValue = width.getValue().intValue();
    }
    final JSlider widthSlider = new JSlider(0, 100, intValue);
    widthSlider.setMajorTickSpacing(10);
    widthSlider.setPaintTicks(true);
    widthSlider.addChangeListener(new InvokeMethodChangeListener(this,
      "setLineWidthValue", panel, widthSlider));
    panel.add(widthSlider);

    final JFormattedTextField widthField = new JFormattedTextField(
      new DecimalFormat("##0"));
    widthField.setColumns(3);
    widthField.setValue(intValue);
    widthField.addActionListener(new InvokeMethodActionListener(this,
      "setLineWidthTextFieldValue", panel, widthField));
    panel.add(widthField);

    panel.addPropertyChangeListener(new InvokeMethodPropertyChangeListener(
      this, "setLineWidthField", panel, widthField));
    panel.addPropertyChangeListener(new InvokeMethodPropertyChangeListener(
      this, "setLineWidthSliderValue", panel, widthSlider));
    SpringLayoutUtil.makeColumns(panel, 2, 0, 0, 5, 5);
    return panel;

  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);
    final Graphics2D graphics = (Graphics2D)g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    /*
     * geometryStyle.setFillStyle(null, graphics);
     * graphics.fill(getLineShape());
     */
    g.translate(getWidth() - 100, 10);

    final Color color = colorField.getColor();
    graphics.setColor(color);

    final float width = widthField.getValue().getValue().floatValue();

    final float dashPhase = 0;
    /*
     * TODO final Measure<Length> strokeDashPhase = stroke.getDashOffset(); if
     * (viewport == null) { dashPhase = strokeDashPhase.getValue().floatValue();
     * } else { dashPhase = (float)viewport.toDisplayValue(strokeDashPhase); }
     */
    final float[] dashArray = null;
    /*
     * TODO final List<Measure<Length>> dashes = stroke.getDashArray(); if
     * (dashes == null) { dashArray = null; } else { dashArray = new
     * float[dashes.size()]; for (int i = 0; i < dashArray.length; i++) { final
     * Measure<Length> dash = dashes.get(i); if (viewport == null) {
     * dashArray[i] = dash.getValue().floatValue(); } else { dashArray[i] =
     * (float)viewport.toDisplayValue(dash); } } }
     */
    // TODO mitre limit
    final int awtLineCap = LineCap.valueOf(
      this.lineCapField.getActionCommand().toString()).getAwtValue();
    final int lineJoin = LineJoin.valueOf(
      this.lineJoinField.getActionCommand().toString()).getAwtValue();
    final BasicStroke basicStroke = new BasicStroke(width, awtLineCap,
      lineJoin, 1, dashArray, dashPhase);
    graphics.setStroke(basicStroke);
    graphics.draw(getLineShape());
  }

  // private Measurable<Length> dashPhase = Measure.valueOf(0, SI.METER);;
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    repaint();
  }

  @Override
  public void save() {
    super.save();
    final GeometryStyle geometryStyle = getValue();
    updateStyle(geometryStyle);
  }

  public void setLineWidthField(final ValuePanel<Measure<Length>> panel,
    final JFormattedTextField field) {
    final Measure<Length> measure = panel.getValue();
    if (measure == null) {
      field.setValue(0);
    } else {
      final Number value = measure.getValue();
      field.setValue(value.intValue());

    }
  }

  public void setLineWidthSliderValue(final ValuePanel<Measure<Length>> panel,
    final JSlider slider) {
    final Measure<Length> measure = panel.getValue();
    if (measure == null) {
      slider.setValue(0);
    } else {
      final Number value = measure.getValue();
      slider.setValue(value.intValue());
    }
  }

  public void setLineWidthTextFieldValue(
    final ValuePanel<Measure<Length>> panel, final JFormattedTextField field) {
    final Measure<Length> measure = panel.getValue();
    final Unit<Length> unit = measure.getUnit();
    final double fieldValue = ((Number)field.getValue()).doubleValue();
    panel.setValue(Measure.valueOf(fieldValue, unit));

  }

  public void setLineWidthValue(final ValuePanel<Measure<Length>> panel,
    final JSlider slider) {
    final Measure<Length> measure = panel.getValue();
    Unit<Length> unit;
    if (measure == null) {
      unit = NonSI.PIXEL;
    } else {
      unit = measure.getUnit();
    }
    final int sliderValue = slider.getValue();
    panel.setValue(Measure.valueOf((double)sliderValue, unit));

  }

  private void updateStyle(final GeometryStyle geometryStyle) {
    final Color color = colorField.getColor();
    geometryStyle.setLineColor(color);

    final CharSequence lineCap = lineCapField.getActionCommand();
    geometryStyle.setLineCap(LineCap.valueOf(lineCap.toString()));

    final CharSequence lineJoin = lineJoinField.getActionCommand();
    geometryStyle.setLineJoin(lineJoin.toString());
    /*
     * final List<Measure<Length>> dash = dashField.getDash();
     * geometryStyle.setDashArray(dash);
     */
    final Measure<Length> width = widthField.getLength();
    geometryStyle.setLineWidth(width);
  }
}
