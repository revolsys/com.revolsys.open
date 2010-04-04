package com.revolsys.jump.ui.info;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.revolsys.jump.ui.builder.FeatureUiBuilder;
import com.revolsys.jump.ui.builder.UiBuilderRegistry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class FeatureViewPanel extends JPanel implements CurrentFeatureListener {
  /**
   * 
   */
  private static final long serialVersionUID = 8343912268601312739L;

  private JEditorPane editorPane = new JEditorPane();

  private Feature feature;

  private Layer layer;

  private FeatureUiBuilder renderer;

  public FeatureViewPanel(final WorkbenchContext context) {
    renderer = new FeatureUiBuilder();
    renderer.setRegistry(UiBuilderRegistry.getInstance(context));
    jbInit();
  }

  void jbInit() {
    JScrollPane scrollPane = new JScrollPane();
    editorPane.setEditable(false);
    editorPane.setText("jEditorPane1");
    editorPane.setContentType("text/html");
    this.setLayout(new BorderLayout());
    this.add(scrollPane, BorderLayout.CENTER);
    scrollPane.getViewport().add(editorPane, null);
  }

  public Color sidebarColor(final Layer layer) {

    Color basicColor;
    if (layer.getBasicStyle().isRenderingFill()) {
      basicColor = layer.getBasicStyle().getFillColor();
    } else {
      basicColor = layer.getBasicStyle().getLineColor();
    }
    int alpha = layer.getBasicStyle().getAlpha();
    return GUIUtil.toSimulatedTransparency(GUIUtil.alphaColor(basicColor, alpha));
  }

  private String toHTML(final Color color) {
    String colorString = "#";
    colorString += pad(Integer.toHexString(color.getRed()));
    colorString += pad(Integer.toHexString(color.getGreen()));
    colorString += pad(Integer.toHexString(color.getBlue()));
    return colorString;
  }

  private String pad(final String s) {
    if (s.length() == 1) {
      return "0" + s;
    } else {
      return s;
    }
  }

  public void updateText() {
    if (feature != null) {
      StringBuffer s = new StringBuffer();
      s.append("<div style=\"padding-left: 5px; background-color: "
        + toHTML(sidebarColor(layer)) + "\">");
      s.append("<div style=\"font-size: 14pt;font-weight:bold;padding: 1px 2px;margin-bottom: 3px; background-color:white\">");
      s.append(layer.getName() + "("
        + layer.getLayerManager().getCategory(layer).getName() + ")");
      s.append("</div>");
      renderer.appendHtml(s, feature, false);
      editorPane.setText(s.toString());
      s.append("</div>");
    } else {
      editorPane.setText("");
    }
    editorPane.setCaretPosition(0);
  }

  public void featureSelected(final Layer layer, final Feature feature) {
    this.layer = layer;
    this.feature = feature;
    updateText();

  }
}
