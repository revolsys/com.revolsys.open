package com.revolsys.swing.dnd;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.jeometry.common.logging.Logs;

public class ClipboardUtil {

  public static Clipboard getClipboard() {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Clipboard clipboard = toolkit.getSystemClipboard();
    return clipboard;
  }

  public static Transferable getContents() {
    final Clipboard clipboard = getClipboard();
    return clipboard.getContents(null);
  }

  @SuppressWarnings("unchecked")
  public static <V> V getContents(final DataFlavor dataFlavor) {
    if (dataFlavor != null) {
      if (isDataFlavorAvailable(dataFlavor)) {
        final Clipboard clipboard = getClipboard();
        try {
          return (V)clipboard.getData(dataFlavor);
        } catch (final UnsupportedFlavorException e) {
          return null;
        } catch (final IOException e) {
          Logs.warn(ClipboardUtil.class,
            "Unable to be clipboard data for flavor=" + dataFlavor.getHumanPresentableName(), e);
        }
      }
    }
    return null;
  }

  public static boolean isDataFlavorAvailable(final DataFlavor dataFlavor) {
    final Clipboard clipboard = getClipboard();
    return clipboard.isDataFlavorAvailable(dataFlavor);
  }

  public static void setContents(final Transferable transferable) {
    final Clipboard clipboard = getClipboard();
    clipboard.setContents(transferable, null);
  }
}
