
package com.revolsys.swing.scripting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.parallel.Invoke;

public class ProcessWindow extends JFrame {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final JTextPane textArea;

  private Process process;

  private final String threadPrefix;

  private final JLabel statusLabel;

  private final JButton closeButton;

  private final JButton stopProcessButton;

  public ProcessWindow(final String title) {
    super(title);
    this.threadPrefix = title.replaceAll("[^A-Za-z0-9_]", "_");
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    SwingUtil.setSize(this, 50, 100);

    this.textArea = new JTextPane();
    this.textArea.setEditable(false);
    this.textArea.setBackground(Color.WHITE);

    final Container contentPane = getContentPane();

    final BasePanel statusPanel = new BasePanel();
    statusPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    contentPane.add(statusPanel, BorderLayout.NORTH);

    this.statusLabel = new JLabel("Running");
    this.statusLabel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(1, 3, 1, 3)));
    statusPanel.add(this.statusLabel);

    contentPane.add(new JScrollPane(this.textArea), BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    buttons.add(RunnableAction.newButton("Copy Output", () -> {
      try {
        final Document document = this.textArea.getDocument();
        final String copyText = document.getText(0, document.getLength());
        final BasicTransferable transferable = new BasicTransferable(copyText, copyText);
        ClipboardUtil.setContents(transferable);
      } catch (final BadLocationException e) {
        Logs.error(this, e);
      }
    }));

    this.stopProcessButton = RunnableAction.newButton("Stop Process", () -> this.stopProcess());
    buttons.add(this.stopProcessButton);

    this.closeButton = RunnableAction.newButton("Close", () -> this.setVisible(false));
    this.closeButton.setEnabled(false);
    buttons.add(this.closeButton);

    contentPane.add(buttons, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(final WindowEvent event) {
        stopProcess();
      }
    });

    setVisible(true);
  }

  public ProcessWindow(final String title, final Process process) {
    this(title);

    setProcess(process);
  }

  public ProcessWindow(final String title, final ProcessBuilder processBuilder) {
    this(title);
    try {
      final Process process = processBuilder.start();
      setProcess(process);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to start process: " + title, e);
    }
  }

  public void newStreamThread(final InputStream in, final String name, final Color color) {
    final JTextPane textArea = this.textArea;
    final StyledDocument doc = (StyledDocument)textArea.getDocument();
    final Style style = doc.addStyle(name, null);
    StyleConstants.setFontFamily(style, "MonoSpaced");
    StyleConstants.setFontSize(style, 12);
    StyleConstants.setForeground(style, color);
    new Thread(() -> {
      final Object sync = new Object();
      try {
        while (this.process.isAlive() || in.available() > 0) {
          try {
            synchronized (sync) {
              sync.wait(500);

            }
          } catch (final InterruptedException ie) {
          }
          final int byteCount = in.available();
          if (byteCount != 0) {
            final byte b[] = new byte[byteCount];
            in.read(b);
            final String input = new String(b, 0, b.length);
            Invoke.later(() -> {
              try {
                doc.insertString(doc.getLength(), input, style);
                textArea.setCaretPosition(textArea.getDocument().getLength());
              } catch (final Exception e) {
                textArea.setText("\nConsole reports an Internal error.");
                textArea.setText("The error is: " + e);
              }
            });
          }
        }
      } catch (final Exception e) {
        return;
      }
    }, this.threadPrefix + "-" + name).start();
  }

  public void setProcess(final Process process) {
    this.process = process;

    final InputStream outStream = this.process.getInputStream();
    newStreamThread(outStream, "Out", Color.BLACK);

    final InputStream errorStream = this.process.getErrorStream();
    newStreamThread(errorStream, "Error", Color.RED);

    process.onExit().thenRun(() -> {
      Invoke.later(() -> {
        this.closeButton.setEnabled(true);
        this.stopProcessButton.setEnabled(false);
        this.statusLabel.setForeground(Color.red);
        this.statusLabel.setText("Terminated");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      });
    });
  }

  public Process stopProcess() {
    return ProcessWindow.this.process.destroyForcibly();
  }

}
