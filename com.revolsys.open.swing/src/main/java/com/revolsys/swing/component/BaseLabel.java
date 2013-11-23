package com.revolsys.swing.component;

import javax.swing.JLabel;

import org.jdesktop.swingx.JXLabel;
import org.springframework.util.StringUtils;

import com.revolsys.swing.SwingUtil;

public class BaseLabel extends JXLabel {

  public BaseLabel() {
    setOpaque(false);
    setFont(SwingUtil.FONT);
    setLineWrap(true);
    setVerticalTextPosition(JLabel.TOP);
    setVerticalAlignment(JLabel.NORTH);

  }

  public BaseLabel(final String text) {
    this();
    setText(text);
  }

  @Override
  public void setText(String text) {
    if (StringUtils.hasText(text)) {
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
