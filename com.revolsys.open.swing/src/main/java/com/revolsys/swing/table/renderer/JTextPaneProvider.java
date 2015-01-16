package com.revolsys.swing.table.renderer;

import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.StringValue;

public class JTextPaneProvider extends ComponentProvider<JTextPane> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public JTextPaneProvider() {
    this(null);
  }

  public JTextPaneProvider(final int alignment) {
    this(null, alignment);
  }

  public JTextPaneProvider(final StringValue converter) {
    this(converter, SwingConstants.LEADING);
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
    this.rendererComponent.setText(getValueAsString(context));
  }

}
