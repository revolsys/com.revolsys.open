package com.revolsys.swing.logging;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.log4j.spi.LoggingEvent;

import com.revolsys.datatype.DataTypes;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class LoggingEventPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private static void addField(final JPanel panel, final String fieldName, final Object value,
    final boolean useScrollPane) {
    addLabel(panel, fieldName);

    String stringValue = DataTypes.toString(value);
    if (!Property.hasValue(stringValue)) {
      stringValue = "-";
    }
    final JTextPane label = new JTextPane();
    label.setContentType("text/html");
    label.setEditable(false);
    label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
    label.setFont(new JLabel().getFont());

    label.setText(stringValue);
    if (useScrollPane) {
      final JScrollPane scrollPane = new JScrollPane(label);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scrollPane.setBorder(BorderFactory.createEtchedBorder());
      panel.add(scrollPane);
    } else {
      final Border border = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(1, 2, 1, 2));
      label.setBorder(border);
      panel.add(label);
    }
  }

  private static void addLabel(final JPanel panel, final String fieldName) {
    final JLabel label = new JLabel(CaseConverter.toCapitalizedWords(fieldName));
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    panel.add(label);
  }

  public static void showDialog(final Component parent, final LoggingEvent event) {
    final JPanel panel = new LoggingEventPanel(event);
    showDialog(parent, "Application Log Details", panel);
  }

  private static void showDialog(final Component parent, final String title, final JPanel panel) {
    final Window window;
    if (parent == null) {
      window = SwingUtil.getActiveWindow();
    } else {
      window = SwingUtilities.getWindowAncestor(parent);
    }
    final JDialog dialog = new JDialog(window, title, ModalityType.APPLICATION_MODAL);
    dialog.setLayout(new BorderLayout());
    dialog.add(panel, BorderLayout.CENTER);
    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(RunnableAction.newButton("OK", () -> dialog.setVisible(false)));
    dialog.add(buttons, BorderLayout.SOUTH);
    dialog.setMaximumSize(new Dimension(1000, 700));
    dialog.pack();
    dialog.setVisible(true);
  }

  public static void showDialog(final Component parent, final String title, final String message,
    final Throwable e) {
    final JPanel panel = new JPanel();
    addField(panel, "Message", message, false);

    final StringBuilderWriter stackTrace = new StringBuilderWriter();
    e.printStackTrace(new PrintWriter(stackTrace));
    addField(panel, "Stack Trace", "<pre>" + stackTrace + "</pre>", true);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    showDialog(parent, title, panel);
  }

  public static void showDialog(final Component parent, final String message, final Throwable e) {
    showDialog(parent, "Error", message, e);
  }

  public LoggingEventPanel(final LoggingEvent event) {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    final long time = event.getTimeStamp();
    final Timestamp timestamp = new Timestamp(time);
    addField(this, "Timestamp", timestamp, false);

    for (final String fieldName : Arrays.asList("level", "loggerName", "message", "threadName")) {

      final Object value = Property.get(event, fieldName);
      addField(this, fieldName, value, false);
    }

    final StringBuilder stackTraceBuilder = new StringBuilder();
    final String[] stack = event.getThrowableStrRep();
    for (final String trace : stack) {
      stackTraceBuilder.append(trace);
      stackTraceBuilder.append("\n");
    }
    final String stackTrace = stackTraceBuilder.toString();
    if (!Property.isEmpty(stackTrace)) {
      addField(this, "Stack Trace", "<pre>" + stackTrace + "</pre>", true);
    }
    GroupLayouts.makeColumns(this, 2, true);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }
}
