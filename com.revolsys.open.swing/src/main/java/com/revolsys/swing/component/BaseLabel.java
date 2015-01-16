package com.revolsys.swing.component;

import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXLabel;

import com.revolsys.swing.SwingUtil;
import com.revolsys.util.Property;

public class BaseLabel extends JXLabel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public BaseLabel() {
    setOpaque(false);
    setFont(SwingUtil.FONT);
    setLineWrap(true);
    setVerticalTextPosition(SwingConstants.TOP);
    setVerticalAlignment(SwingConstants.NORTH);

  }

  public BaseLabel(final String text) {
    this();
    setText(text);
  }

  @Override
  public void setText(String text) {
    if (Property.hasValue(text)) {
      if (!text.startsWith("<html")) {
        if (text.startsWith("<")) {
          text = "<html>" + text + "</html>";
        } else {
          text = "<html><p>" + text + "</p></html>";
        }
      }
    }
    super.setText(text);
  }

}
