package com.revolsys.swing.table.renderer;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.StringValue;

public class JTextPaneProvider extends ComponentProvider<JTextPane> {

  public JTextPaneProvider() {
    this(null);
  }

  public JTextPaneProvider(final int alignment) {
    this(null, alignment);
  }

  public JTextPaneProvider(final StringValue converter) {
    this(converter, JLabel.LEADING);
  }

  public JTextPaneProvider(final StringValue converter, final int alignment) {
    super(converter, alignment);
  }

  @Override
  protected void configureState(final CellContext context) {
    // rendererComponent.setAlignmentX(JTextArea.);HorizontalAlignment(getHorizontalAlignment());
  }

  @Override
  protected JRendererTextPane createRendererComponent() {
    return new JRendererTextPane();
  }

  @Override
  protected void format(final CellContext context) {
    // rendererComponent.setIcon(getValueAsIcon(context));
    rendererComponent.setText(getValueAsString(context));
  }

}
