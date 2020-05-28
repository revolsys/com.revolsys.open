package com.revolsys.swing.logging;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
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
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

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

  public static String getStackTrace(final ILoggingEvent event) {
    final IThrowableProxy thrown = event.getThrowableProxy();
    return getStackTrace(thrown);
  }

  static String getStackTrace(IThrowableProxy thrown) {
    if (thrown == null) {
      return null;
    } else {
      final LoggingEvent event = new LoggingEvent();
      event.setThrowableProxy((ThrowableProxy)thrown);
      final StringBuilder string = new StringBuilder();
      do {
        recursiveAppend(string, null, 1, thrown);
        thrown = thrown.getCause();
      } while (thrown != null);
      return string.toString();
    }
  }

  private static void recursiveAppend(final StringBuilder sb, final String prefix, final int indent,
    final IThrowableProxy tp) {
    if (tp == null) {
      return;
    }
    subjoinFirstLine(sb, prefix, indent, tp);
    sb.append(CoreConstants.LINE_SEPARATOR);
    subjoinSTEPArray(sb, indent, tp);
    final IThrowableProxy[] suppressed = tp.getSuppressed();
    if (suppressed != null) {
      for (final IThrowableProxy current : suppressed) {
        recursiveAppend(sb, CoreConstants.SUPPRESSED,
          indent + ThrowableProxyUtil.SUPPRESSED_EXCEPTION_INDENT, current);
      }
    }
    recursiveAppend(sb, CoreConstants.CAUSED_BY, indent, tp.getCause());
  }

  public static void showDialog(final ILoggingEvent event) {
    final long time = event.getTimeStamp();
    final Timestamp timestamp = new Timestamp(time);

    final String stackTrace = getStackTrace(event);

    final Level level = event.getLevel();
    final String loggerName = event.getLoggerName();
    final String threadName = event.getThreadName();
    final Object message = event.getFormattedMessage();
    showDialog(timestamp, level, loggerName, message, threadName, stackTrace);
  }

  public static void showDialog(final List<Object> event) {
    final Timestamp timestamp = (Timestamp)event.get(0);
    final Level level = (Level)event.get(1);
    final String loggerName = (String)event.get(2);
    final String message = (String)event.get(3);
    final String threadName = (String)event.get(4);
    final String stackTrace = getStackTrace((ThrowableProxy)event.get(5)); // TODO
    showDialog(timestamp, level, loggerName, message, threadName, stackTrace);
  }

  public static void showDialog(final String title, final String message, final Throwable e) {
    Invoke.later(() -> {
      final StringBuilderWriter stackTrace = new StringBuilderWriter();
      try (
        PrintWriter printWriter = new PrintWriter(stackTrace)) {
        e.printStackTrace(printWriter);
      }

      final LoggingEventPanel panel = new LoggingEventPanel(null, null, null, null, message,
        stackTrace);

      panel.showDialog(title);
    });
  }

  public static void showDialog(final String message, final Throwable e) {
    showDialog("Error", message, e);
  }

  public static void showDialog(final Timestamp timestamp, final Level level,
    final String loggerName, final Object message, final String threadName,
    final String stackTrace) {
    if (SwingUtilities.isEventDispatchThread()) {
      final LoggingEventPanel panel = new LoggingEventPanel(timestamp, level, loggerName,
        threadName, message, stackTrace);
      panel.showDialog("Application Log Details");
    } else {
      Invoke.later(() -> {
        showDialog(timestamp, level, loggerName, message, threadName, stackTrace);
      });
    }
  }

  private static void subjoinExceptionMessage(final StringBuilder buf, final IThrowableProxy tp) {
    final String className = tp.getClassName();
    final String message = tp.getMessage();
    buf.append(className).append(": ").append(message);
  }

  private static void subjoinFirstLine(final StringBuilder buf, final String prefix,
    final int indent, final IThrowableProxy tp) {
    ThrowableProxyUtil.indent(buf, indent - 1);
    if (prefix != null) {
      buf.append(prefix);
    }
    subjoinExceptionMessage(buf, tp);
  }

  private static void subjoinSTEPArray(final StringBuilder buf, final int indent,
    final IThrowableProxy tp) {
    final StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
    final int commonFrames = tp.getCommonFrames();

    int maxIndex = stepArray.length;
    if (commonFrames > 0) {
      maxIndex -= commonFrames;
    }

    for (int i = 0; i < maxIndex; i++) {
      final StackTraceElementProxy element = stepArray[i];
      ThrowableProxyUtil.indent(buf, indent);
      buf.append(element);
      buf.append(CoreConstants.LINE_SEPARATOR);
    }

    if (commonFrames > 0) {
      ThrowableProxyUtil.indent(buf, indent);
      buf.append("... ")
        .append(tp.getCommonFrames())
        .append(" common frames omitted")
        .append(CoreConstants.LINE_SEPARATOR);
    }
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
      addField(this, "Stack Trace", stackTrace, true);
      if (this.copyText.length() > 0) {
        this.copyText.append("\n");
      }
      this.copyText.append(stackTrace);
    }

    GroupLayouts.makeColumns(this, 2, true);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private boolean isIgnoredStackTraceLine(final String line) {
    return false;
  }

  private void showDialog(final String title) {
    final JDialog dialog = Dialogs.newDocumentModal(title);
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
    SwingUtil.autoAdjustSize(dialog);
    dialog.setVisible(true);
  }
}
