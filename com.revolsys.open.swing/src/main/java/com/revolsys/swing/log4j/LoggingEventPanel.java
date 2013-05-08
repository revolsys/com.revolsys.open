package com.revolsys.swing.log4j;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.spi.LoggingEvent;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;

public class LoggingEventPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  public static void showDialog(Component component, LoggingEvent event) {
    Window window = SwingUtilities.getWindowAncestor(component);
    JDialog dialog = new JDialog(window, "Application Log Details",
      ModalityType.APPLICATION_MODAL);
    dialog.setLayout(new BorderLayout());
    dialog.add(new LoggingEventPanel(event), BorderLayout.CENTER);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(InvokeMethodAction.createButton("OK", dialog, "setVisible",
      false));
    dialog.add(buttons, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setVisible(true);
  }

  public LoggingEventPanel(LoggingEvent event) {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setLayout(new GroupLayout(this));
    long time = event.getTimeStamp();
    Timestamp timestamp = new Timestamp(time);
    addField("Timestamp", timestamp);

    for (String fieldName : Arrays.asList("level", "loggerName", "message",
      "threadName")) {

      Object value = JavaBeanUtil.getValue(event, fieldName);
      addField(fieldName, value);
    }

    addLabel("Stack Trace");
    String[] stack = event.getThrowableStrRep();
    if (stack != null) {
      JXTextArea textArea = SwingUtil.createTextArea(
        Math.min(20, stack.length), 80);
      textArea.setEditable(false);
      for (String trace : stack) {
        textArea.append(trace);
        textArea.append("\n");
      }
      add(new JScrollPane(textArea));
      textArea.setCaretPosition(0);
    }
    GroupLayoutUtil.makeColumns(this, 2);
  }

  private void addField(String fieldName, Object value) {
    addLabel(fieldName);

    String stringValue = StringConverterRegistry.toString(value);
    if (!StringUtils.hasText(stringValue)) {
      stringValue = "-";
    }
    JXTextField field = SwingUtil.createTextField(Math.min(80,
      stringValue.length()));
    field.setEditable(false);
    field.setText(stringValue);
    add(field);
  }

  private void addLabel(String fieldName) {
    final JLabel label = new JLabel(CaseConverter.toCapitalizedWords(fieldName));
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    add(label);
  }
}
