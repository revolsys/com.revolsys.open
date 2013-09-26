package com.revolsys.swing.logging;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.spi.LoggingEvent;
import org.jdesktop.swingx.JXTextArea;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class LoggingEventPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final Component component,
    final LoggingEvent event) {
    final Window window;
    if (component == null) {
      window = SwingUtil.getActiveWindow();
    } else {
      window = SwingUtilities.getWindowAncestor(component);
    }
    final JDialog dialog = new JDialog(window, "Application Log Details",
      ModalityType.APPLICATION_MODAL);
    dialog.setLayout(new BorderLayout());
    dialog.add(new LoggingEventPanel(event), BorderLayout.CENTER);
    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(InvokeMethodAction.createButton("OK", dialog, "setVisible",
      false));
    dialog.add(buttons, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setVisible(true);
  }

  public LoggingEventPanel(final LoggingEvent event) {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    final long time = event.getTimeStamp();
    final Timestamp timestamp = new Timestamp(time);
    addField("Timestamp", timestamp);

    for (final String fieldName : Arrays.asList("level", "loggerName",
      "message", "threadName")) {

      final Object value = Property.get(event, fieldName);
      addField(fieldName, value);
    }

    addLabel("Stack Trace");
    final String[] stack = event.getThrowableStrRep();
    if (stack != null) {
      final JXTextArea textArea = SwingUtil.createTextArea(
        Math.min(20, stack.length), 80);
      textArea.setEditable(false);
      for (final String trace : stack) {
        textArea.append(trace);
        textArea.append("\n");
      }
      add(new JScrollPane(textArea));
      textArea.setCaretPosition(0);
    }
    GroupLayoutUtil.makeColumns(this, 2, true);
  }

  private void addField(final String fieldName, final Object value) {
    addLabel(fieldName);

    String stringValue = StringConverterRegistry.toString(value);
    if (!StringUtils.hasText(stringValue)) {
      stringValue = "-";
    }
    final TextField field = SwingUtil.createTextField(Math.min(80,
      stringValue.length()));
    field.setEditable(false);
    field.setText(stringValue);
    add(field);
  }

  private void addLabel(final String fieldName) {
    final JLabel label = new JLabel(CaseConverter.toCapitalizedWords(fieldName));
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    add(label);
  }
}
