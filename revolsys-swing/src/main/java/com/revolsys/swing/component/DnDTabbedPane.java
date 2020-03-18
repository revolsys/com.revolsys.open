package com.revolsys.swing.component;

/** MODIFIED DnDTabbedPane.java
 * http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
 * originally written by Terai Atsuhiro.
 * so that tabs can be transfered from one pane to another.
 * eed3si9n.
 */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.revolsys.io.FileUtil;
import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.parallel.Invoke;

public class DnDTabbedPane extends TabbedPane {
  class CDropTargetListener implements DropTargetListener {
    @Override
    public void dragEnter(final DropTargetDragEvent e) {
      if (isDragAcceptable(e)) {
        final int dropAction = e.getDropAction();
        e.acceptDrag(dropAction);
      } else {
        e.rejectDrag();
      }
    }

    @Override
    public void dragExit(final DropTargetEvent e) {
      DnDTabbedPane.this.isDrawRect = false;
    }

    @Override
    public void dragOver(final DropTargetDragEvent e) {
      final TabTransferData data = getTabTransferData(e);
      if (data != null) {
        final int tabPlacement = getTabPlacement();
        final Point location = e.getLocation();
        final int targetTabIndex = getTargetTabIndex(location);
        if (tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM) {
          initTargetLeftRightLine(targetTabIndex, data);
        } else {
          initTargetTopBottomLine(targetTabIndex, data);
        }

        repaint();
        if (hasGhost()) {
          final Point ghostLocation = buildGhostLocation(location);
          glassPane.setPoint(ghostLocation);
          glassPane.repaint();
        }
      }
    }

    @Override
    public void drop(final DropTargetDropEvent e) {
      if (isDropAcceptable(e)) {
        moveTab(getTabTransferData(e), getTargetTabIndex(e.getLocation()));
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }

      DnDTabbedPane.this.isDrawRect = false;
      repaint();
    }

    @Override
    public void dropActionChanged(final DropTargetDragEvent e) {
    }

    public boolean isDragAcceptable(final DropTargetDragEvent e) {
      final Transferable t = e.getTransferable();
      if (t == null) {
        return false;
      }

      final DataFlavor[] flavor = e.getCurrentDataFlavors();
      if (!t.isDataFlavorSupported(flavor[0])) {
        return false;
      }

      final TabTransferData data = getTabTransferData(e);
      if (data != null) {
        final DnDTabbedPane sourceTabs = data.getTabbedPane();
        if (DnDTabbedPane.this == sourceTabs && data.getTabIndex() >= 0) {
          return true;
        }

        if (DnDTabbedPane.this != sourceTabs) {
          if (DnDTabbedPane.this.acceptor != null) {
            return DnDTabbedPane.this.acceptor.isDropAcceptable(sourceTabs, data.getTabIndex());
          }
        }
      }
      return false;
    }

    public boolean isDropAcceptable(final DropTargetDropEvent e) {
      final Transferable t = e.getTransferable();
      if (t == null) {
        return false;
      }

      final DataFlavor[] flavor = e.getCurrentDataFlavors();
      if (!t.isDataFlavorSupported(flavor[0])) {
        return false;
      }

      final TabTransferData data = getTabTransferData(e);

      final DnDTabbedPane sourceTabs = data.getTabbedPane();
      if (DnDTabbedPane.this == sourceTabs && data.getTabIndex() >= 0) {
        return true;
      }

      if (DnDTabbedPane.this != sourceTabs) {
        if (DnDTabbedPane.this.acceptor != null) {
          return DnDTabbedPane.this.acceptor.isDropAcceptable(sourceTabs, data.getTabIndex());
        }
      }

      return false;
    }
  }

  public interface TabAcceptor {
    boolean isDropAcceptable(DnDTabbedPane component, int index);
  }

  class TabTransferable implements Transferable {
    private TabTransferData data = null;

    public TabTransferable(final DnDTabbedPane tabbedPane, final int tabIndex) {
      this.data = new TabTransferData(DnDTabbedPane.this, tabIndex);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) {
      return this.data;
      // return DnDTabbedPane.this;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      final DataFlavor[] f = new DataFlavor[1];
      f[0] = DnDTabbedPane.this.FLAVOR;
      return f;
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
      return flavor.getHumanPresentableName().equals(NAME);
    }
  }

  class TabTransferData {
    private DnDTabbedPane tabbedPane = null;

    private int tabIndex = -1;

    public TabTransferData() {
    }

    public TabTransferData(final DnDTabbedPane tabbedPane, final int tabIndex) {
      this.tabbedPane = tabbedPane;
      this.tabIndex = tabIndex;
    }

    public DnDTabbedPane getTabbedPane() {
      return this.tabbedPane;
    }

    public int getTabIndex() {
      return this.tabIndex;
    }

    public void setTabbedPane(final DnDTabbedPane pane) {
      this.tabbedPane = pane;
    }

    public void setTabIndex(final int index) {
      this.tabIndex = index;
    }
  }

  private static GhostGlassPane glassPane = new GhostGlassPane();

  private static final int LINEWIDTH = 3;

  private static final String NAME = "TabTransferData";

  public static final long serialVersionUID = 1L;

  private TabAcceptor acceptor = null;

  private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);

  private boolean hasGhost = false;

  private boolean isDrawRect = false;

  private final Color lineColor = new Color(0, 100, 255);

  private final Rectangle2D lineRect = new Rectangle2D.Double();

  private boolean movingTab = false;

  public DnDTabbedPane() {
    super();
    final DragSourceListener dsl = new DragSourceListener() {
      @Override
      public void dragDropEnd(final DragSourceDropEvent e) {
        DnDTabbedPane.this.isDrawRect = false;
        DnDTabbedPane.this.lineRect.setRect(0, 0, 0, 0);
        // dragTabIndex = -1;

        if (hasGhost()) {
          glassPane.setVisible(false);
          glassPane.setImage(null);
        }
      }

      @Override
      public void dragEnter(final DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      }

      @Override
      public void dragExit(final DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        DnDTabbedPane.this.lineRect.setRect(0, 0, 0, 0);
        DnDTabbedPane.this.isDrawRect = false;
        glassPane.setPoint(new Point(-1000, -1000));
        glassPane.repaint();
      }

      @Override
      public void dragOver(final DragSourceDragEvent e) {
        // e.getLocation()
        // This method returns a Point indicating the cursor location in screen
        // coordinates at the moment

        final TabTransferData data = getTabTransferData(e);
        if (data == null) {
          e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
          return;
        }

        /*
         * Point tabPt = e.getLocation();
         * SwingUtilities.convertPointFromScreen(tabPt, DnDTabbedPane.this); if
         * (DnDTabbedPane.this.contains(tabPt)) { int targetIdx =
         * getTargetTabIndex(tabPt); int sourceIndex = data.getTabIndex(); if
         * (getTabAreaBound().contains(tabPt) && (targetIdx >= 0) && (targetIdx
         * != sourceIndex) && (targetIdx != sourceIndex + 1)) {
         * e.getDragSourceContext().setCursor( DragSource.DefaultMoveDrop);
         * return; } e.getDragSourceContext().setCursor(
         * DragSource.DefaultMoveNoDrop); return; }
         */

        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      }

      @Override
      public void dropActionChanged(final DragSourceDragEvent e) {
      }
    };

    final DragGestureListener dgl = new DragGestureListener() {
      @Override
      public void dragGestureRecognized(final DragGestureEvent e) {
        final Point tabPt = e.getDragOrigin();
        final int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
        if (dragTabIndex < 0) {
          return;
        }

        initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);
        try {
          e.startDrag(DragSource.DefaultMoveDrop,
            new TabTransferable(DnDTabbedPane.this, dragTabIndex), dsl);
        } catch (final InvalidDnDOperationException idoe) {
          idoe.printStackTrace();
        }
      }
    };

    // dropTarget =
    new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
    new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,
      dgl);
    this.acceptor = new TabAcceptor() {
      @Override
      public boolean isDropAcceptable(final DnDTabbedPane component, final int index) {
        return true;
      }
    };
  }

  private Point buildGhostLocation(final Point location) {
    Point retval = new Point(location);

    switch (getTabPlacement()) {
      case SwingConstants.TOP: {
        retval.y = 1;
        retval.x -= glassPane.getGhostWidth() / 2;
      }
      break;

      case SwingConstants.BOTTOM: {
        retval.y = getHeight() - 1 - glassPane.getGhostHeight();
        retval.x -= glassPane.getGhostWidth() / 2;
      }
      break;

      case SwingConstants.LEFT: {
        retval.x = 1;
        retval.y -= glassPane.getGhostHeight() / 2;
      }
      break;

      case SwingConstants.RIGHT: {
        retval.x = getWidth() - 1 - glassPane.getGhostWidth();
        retval.y -= glassPane.getGhostHeight() / 2;
      }
      break;
    } // switch

    retval = SwingUtilities.convertPoint(DnDTabbedPane.this, retval, glassPane);
    return retval;
  }

  public TabAcceptor getAcceptor() {
    return this.acceptor;
  }

  private TabTransferData getTabTransferData(final DragSourceDragEvent event) {
    try {
      final TabTransferData data = (TabTransferData)event.getDragSourceContext()
        .getTransferable()
        .getTransferData(this.FLAVOR);
      return data;
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private TabTransferData getTabTransferData(final DropTargetDragEvent event) {
    try {
      final Transferable transferable = event.getTransferable();
      if (transferable.isDataFlavorSupported(this.FLAVOR)) {
        final TabTransferData data = (TabTransferData)transferable.getTransferData(this.FLAVOR);
        return data;
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private TabTransferData getTabTransferData(final DropTargetDropEvent event) {
    try {
      final TabTransferData data = (TabTransferData)event.getTransferable()
        .getTransferData(this.FLAVOR);
      return data;
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * returns potential index for drop.
   * @param point point given in the drop site component's coordinate
   * @return returns potential index for drop.
   */
  private int getTargetTabIndex(final Point point) {
    final int tabPlacement = getTabPlacement();
    final boolean isTopOrBottom = tabPlacement == SwingConstants.TOP
      || tabPlacement == SwingConstants.BOTTOM;

    // if the pane is empty, the target index is always zero.
    if (getTabCount() == 0) {
      return 0;
    }

    for (int i = 0; i < getTabCount(); i++) {
      final Rectangle r = getBoundsAt(i);
      if (isTopOrBottom) {
        r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
      } else {
        r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
      }

      if (r.contains(point)) {
        return i;
      }
    }

    final Rectangle r = getBoundsAt(getTabCount() - 1);
    if (isTopOrBottom) {
      final int x = r.x + r.width / 2;
      r.setRect(x, r.y, getWidth() - x, r.height);
    } else {
      final int y = r.y + r.height / 2;
      r.setRect(r.x, y, r.width, getHeight() - y);
    }

    return r.contains(point) ? getTabCount() : -1;
  }

  public boolean hasGhost() {
    return this.hasGhost;
  }

  private void initGlassPane(final Component c, final Point tabPt, final int tabIndex) {
    // Point p = (Point) pt.clone();
    getRootPane().setGlassPane(glassPane);
    if (hasGhost()) {
      final Rectangle rect = getBoundsAt(tabIndex);
      BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(),
        BufferedImage.TYPE_INT_ARGB);
      final Graphics g = image.getGraphics();
      c.paint(g);
      image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
      glassPane.setImage(image);
    }

    glassPane.setPoint(buildGhostLocation(tabPt));
    glassPane.setVisible(true);
  }

  private void initTargetLeftRightLine(final int next, final TabTransferData data) {
    if (next < 0) {
      this.lineRect.setRect(0, 0, 0, 0);
      this.isDrawRect = false;
      return;
    }

    final DnDTabbedPane tabs = data.getTabbedPane();
    final int tabIndex = data.getTabIndex();
    final int tabCount = getTabCount();
    if (tabs == this && (tabIndex == next || next - tabIndex == 1)) {
      this.lineRect.setRect(0, 0, 0, 0);
      this.isDrawRect = false;
    } else if (tabCount == 0) {
      this.lineRect.setRect(0, 0, 0, 0);
      this.isDrawRect = false;
      return;
    } else if (next == 0) {
      final Rectangle rect = getBoundsAt(0);
      this.lineRect.setRect(-LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
      this.isDrawRect = true;
    } else if (next == tabCount) {
      final Rectangle rect = getBoundsAt(tabCount - 1);
      this.lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
      this.isDrawRect = true;
    } else {
      final Rectangle rect = getBoundsAt(next - 1);
      this.lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
      this.isDrawRect = true;
    }
  }

  private void initTargetTopBottomLine(final int next, final TabTransferData data) {
    if (next < 0) {
      this.lineRect.setRect(0, 0, 0, 0);
      this.isDrawRect = false;
      return;
    }

    if (data.getTabbedPane() == this
      && (data.getTabIndex() == next || next - data.getTabIndex() == 1)) {
      this.lineRect.setRect(0, 0, 0, 0);
      this.isDrawRect = false;
    } else if (getTabCount() == 0) {
      this.lineRect.setRect(0, 0, 0, 0);
      this.isDrawRect = false;
      return;
    } else if (next == getTabCount()) {
      final Rectangle rect = getBoundsAt(getTabCount() - 1);
      this.lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
      this.isDrawRect = true;
    } else if (next == 0) {
      final Rectangle rect = getBoundsAt(0);
      this.lineRect.setRect(rect.x, -LINEWIDTH / 2, rect.width, LINEWIDTH);
      this.isDrawRect = true;
    } else {
      final Rectangle rect = getBoundsAt(next - 1);
      this.lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
      this.isDrawRect = true;
    }
  }

  public boolean isMovingTab() {
    return this.movingTab;
  }

  public void moveTab(final int sourceIndex, final int targetIndex) {
    Invoke.later(() -> {
      this.movingTab = true;
      try {
        final int tabCount = getTabCount();
        if (sourceIndex >= 0 && targetIndex >= 0 && sourceIndex != targetIndex
          && sourceIndex < tabCount) {
          int destIndex = targetIndex;
          if (sourceIndex < destIndex) {
            destIndex -= 1;
          }
          final Component component = getComponentAt(sourceIndex);
          final String title = getTitleAt(sourceIndex);
          final Icon icon = getIconAt(sourceIndex);
          final String toolTip = getToolTipTextAt(sourceIndex);
          final Component tabComponent = getTabComponentAt(sourceIndex);
          superRemove(sourceIndex);

          insertTab(title, icon, component, toolTip, destIndex);
          setSelectedIndex(destIndex);
          setTabComponentAt(destIndex, tabComponent);
        }
      } finally {
        this.movingTab = false;
      }
    });
  }

  private void moveTab(final TabTransferData data, int targetIndex) {
    final DnDTabbedPane sourceTabs = data.getTabbedPane();
    final int sourceIndex = data.getTabIndex();
    if (sourceIndex >= 0) {
      final Component component = sourceTabs.getComponentAt(sourceIndex);
      final String title = sourceTabs.getTitleAt(sourceIndex);
      final Icon icon = sourceTabs.getIconAt(sourceIndex);
      final String toolTip = sourceTabs.getToolTipTextAt(sourceIndex);
      final Component tabComponent = sourceTabs.getTabComponentAt(sourceIndex);
      if (this == sourceTabs) {
        moveTab(sourceIndex, targetIndex);
      } else {
        sourceTabs.superRemove(sourceIndex);
        if (targetIndex < 0) {
          targetIndex = 0;
        }
        insertTab(title, icon, component, toolTip, targetIndex);
        setSelectedIndex(targetIndex);
        setTabComponentAt(targetIndex, tabComponent);
      }
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    if (this.isDrawRect) {
      final Graphics2D g2 = (Graphics2D)g;
      g2.setPaint(this.lineColor);
      g2.fill(this.lineRect);
    }
  }

  @Override
  public void remove(final int index) {
    final Component component = getComponentAt(index);
    superRemove(index);
    if (component instanceof AutoCloseable) {
      FileUtil.closeSilent((AutoCloseable)component);
    }
  }

  public void setAcceptor(final TabAcceptor value) {
    this.acceptor = value;
  }

  public void setPaintGhost(final boolean flag) {
    this.hasGhost = flag;
  }

  protected void superRemove(final int index) {
    super.remove(index);
  }
}

class GhostGlassPane extends JPanel {
  private static final AlphaComposite COMPOSITE = AlphaComposite
    .getInstance(AlphaComposite.SRC_OVER, 0.7f);

  public static final long serialVersionUID = 1L;

  private BufferedImage draggingGhost = null;

  private int x;

  private int y;

  public GhostGlassPane() {
    setOpaque(false);
  }

  public int getGhostHeight() {
    if (this.draggingGhost == null) {
      return 0;
    }

    return this.draggingGhost.getHeight(this);
  }

  public int getGhostWidth() {
    if (this.draggingGhost == null) {
      return 0;
    }

    return this.draggingGhost.getWidth(this);
  }

  @Override
  public void paintComponent(final Graphics g) {
    if (this.draggingGhost == null) {
      return;
    }

    final Graphics2D g2 = (Graphics2D)g;
    g2.setComposite(COMPOSITE);

    g2.drawImage(this.draggingGhost, this.x, this.y, null);
  }

  public void setImage(final BufferedImage draggingGhost) {
    this.draggingGhost = draggingGhost;
  }

  public void setPoint(final Point location) {
    this.x = location.x;
    this.y = location.y;
  }
}
