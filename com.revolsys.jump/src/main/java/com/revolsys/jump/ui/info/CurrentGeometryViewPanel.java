package com.revolsys.jump.ui.info;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

@SuppressWarnings("serial")
public class CurrentGeometryViewPanel extends JPanel implements
  CurrentFeatureListener {
  private JEditorPane editorPane = new JEditorPane();

  private Feature feature;

  private LayerTitlePanel title;

  private JScrollPane scrollPane;

  public CurrentGeometryViewPanel(final WorkbenchContext context) {
    super(new BorderLayout());

    title = new LayerTitlePanel();
    this.add(title, BorderLayout.NORTH);
    title.setBackground(Color.WHITE);

    scrollPane = new JScrollPane();
    this.add(scrollPane, BorderLayout.CENTER);
    editorPane.setEditable(false);
    editorPane.setText("jEditorPane1");
    editorPane.setContentType("text/html");
    scrollPane.getViewport().add(editorPane, null);
  }

  public void updateText() {
    if (feature != null) {
      StringBuffer s = new StringBuffer();
      Geometry geometry = feature.getGeometry();
      s.append("<pre>");
      WKTWriter writer = new WKTWriter(3);
      writer.setMaxCoordinatesPerLine(1);
      writer.setFormatted(true);
      s.append(writer.writeFormatted(geometry));
      s.append("</pre>");

      editorPane.setText(s.toString());
    } else {
      editorPane.setText("");
    }
    editorPane.setCaretPosition(0);
  }

  public void featureSelected(final Layer layer, final Feature feature) {
    this.feature = feature;
    title.setLayer(layer);
    updateText();

  }
}
