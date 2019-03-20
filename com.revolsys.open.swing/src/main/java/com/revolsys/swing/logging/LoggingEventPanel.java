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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;

import com.revolsys.datatype.DataTypes;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class LoggingEventPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private static void addField(final JPanel panel, final String fieldName, final Object value,
    final boolean useScrollPane) {
    if (Property.hasValue(value)) {
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

      label.setText("<pre>" + stringValue + "</pre>");
      if (useScrollPane) {
        final JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        panel.add(scrollPane);
      } else {
        final Border border = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
          BorderFactory.createEmptyBorder(1, 2, 1, 2));
        label.setBorder(border);
        panel.add(label);
      }
    }
  }

  private static void addLabel(final JPanel panel, final String fieldName) {
    final JLabel label = new JLabel(CaseConverter.toCapitalizedWords(fieldName));
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    panel.add(label);
  }

  public static String getStackTrace(final LogEvent event) {
    final ThrowableProxy thrown = event.getThrownProxy();
    return getStackTrace(thrown);
  }

  static String getStackTrace(final ThrowableProxy thrown) {
    if (thrown == null) {
      return null;
    } else {
      return thrown.getExtendedStackTraceAsString();
    }
  }

  public static void showDialog(final Component parent, final List<Object> event) {
    final Timestamp timestamp = (Timestamp)event.get(0);
    final Level level = (Level)event.get(1);
    final String loggerName = (String)event.get(2);
    final String message = (String)event.get(3);
    final String threadName = (String)event.get(4);
    final String stackTrace = getStackTrace((ThrowableProxy)event.get(5)); // TODO
    showDialog(parent, timestamp, level, loggerName, message, threadName, stackTrace);
  }

  public static void showDialog(final Component parent, final LogEvent event) {
    final long time = event.getTimeMillis();
    final Timestamp timestamp = new Timestamp(time);

    final String stackTrace = getStackTrace(event);

    final Level level = event.getLevel();
    final String loggerName = event.getLoggerName();
    final String threadName = event.getThreadName();
    final Object message = event.getMessage();
    showDialog(parent, timestamp, level, loggerName, message, threadName, stackTrace);
  }

  public static void showDialog(final Component parent, final String title, final String message,
    final Throwable e) {
    Invoke.later(() -> {
      final StringBuilderWriter stackTrace = new StringBuilderWriter();
      try (
        PrintWriter printWriter = new PrintWriter(stackTrace)) {
        e.printStackTrace(printWriter);
      }

      final LoggingEventPanel panel = new LoggingEventPanel(null, null, null, null, message,
        stackTrace);

      panel.showDialog(parent, title);
    });
  }

  public static void showDialog(final Component parent, final String message, final Throwable e) {
    showDialog(parent, "Error", message, e);
  }

  public static void showDialog(final Component parent, final Timestamp timestamp,
    final Level level, final String loggerName, final Object message, final String threadName,
    final String stackTrace) {
    Invoke.later(() -> {
      final LoggingEventPanel panel = new LoggingEventPanel(timestamp, level, loggerName,
        threadName, message, stackTrace);
      panel.showDialog(parent, "Application Log Details");
    });
  }

  private final StringBuilder copyText = new StringBuilder();

  public LoggingEventPanel(final Timestamp time, final Object level, final String loggerName,
    final String threadName, final Object message, final Object stackTrace) {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    addField(this, "Timestamp", time, false);
    addField(this, "level", level, false);
    addField(this, "loggerName", loggerName, false);
    addField(this, "message", message, false);
    addField(this, "threadName", threadName, false);

    if (message != null) {
      this.copyText.append(message);
    }

    if (Property.hasValue(stackTrace)) {
      addField(this, "Stack Trace", "<pre>" + stackTrace + "</pre>", true);
      if (this.copyText.length() > 0) {
        this.copyText.append("\n");
      }
      this.copyText.append(stackTrace);
    }

    GroupLayouts.makeColumns(this, 2, true);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private void showDialog(final Component parent, final String title) {
    final Window window;
    if (parent == null) {
      window = SwingUtil.getActiveWindow();
    } else {
      window = SwingUtilities.getWindowAncestor(parent);
    }
    final JDialog dialog = new JDialog(window, title, ModalityType.APPLICATION_MODAL);
    dialog.setLayout(new BorderLayout());
    dialog.add(this, BorderLayout.CENTER);
    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    buttons.add(RunnableAction.newButton("Copy Error", () -> {
      final BasicTransferable transferable = new BasicTransferable(this.copyText.toString(),
        "<pre>" + this.copyText + "</pre>");
      ClipboardUtil.setContents(transferable);
    }));
    buttons.add(RunnableAction.newButton("OK", () -> dialog.setVisible(false)));
    dialog.add(buttons, BorderLayout.SOUTH);
    dialog.setMaximumSize(new Dimension(1000, 700));
    dialog.pack();
    dialog.setVisible(true);
  }
}
