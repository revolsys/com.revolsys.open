package com.revolsys.jtstest.testbuilder.ui.tools;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.jts.geom.Coordinates;

public abstract class LineBandTool extends IndicatorTool {
  private final List<Coordinates> coordinates = new ArrayList<Coordinates>(); // in
                                                                              // model
                                                                              // space

  protected Coordinates tentativeCoordinate;

  // set this to true if band should be closed
  private boolean closeRing = false;

  private int clickCountToFinish = 2;

  private boolean drawBandLines = true;

  public LineBandTool() {
    super();
  }

  protected void add(final Coordinates c) {
    // don't add repeated coords
    if (coordinates.size() > 0
      && c.equals2d(coordinates.get(coordinates.size() - 1))) {
      return;
    }
    coordinates.add(c);
  }

  protected abstract void bandFinished() throws Exception;

  private void drawVertices(final GeneralPath path) {
    for (int i = 0; i < coordinates.size(); i++) {
      final Coordinates coord = coordinates.get(i);
      final Point2D p = toView(coord);
      path.moveTo((int)p.getX() - 2, (int)p.getY() - 2);
      path.lineTo((int)p.getX() + 2, (int)p.getY() - 2);
      path.lineTo((int)p.getX() + 2, (int)p.getY() + 2);
      path.lineTo((int)p.getX() - 2, (int)p.getY() + 2);
      path.lineTo((int)p.getX() - 2, (int)p.getY() - 2);
    }

  }

  protected void finishGesture() throws Exception {
    clearIndicator();
    try {
      bandFinished();
    } finally {
      coordinates.clear();
    }
  }

  /**
   * Returns an empty List once the shape is cleared.
   * 
   * @see LineBandTool#clearShape
   */
  public List getCoordinateArray() {
    return Collections.unmodifiableList(coordinates);
  }

  @Override
  protected Shape getShape() {
    if (coordinates.isEmpty()) {
      return null;
    }
    final Point2D firstPoint = toView(coordinates.get(0));
    final GeneralPath path = new GeneralPath();
    path.moveTo((float)firstPoint.getX(), (float)firstPoint.getY());
    if (!drawBandLines) {
      return path;
    }

    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinates nextCoordinate = coordinates.get(i);
      final Point2D nextPoint = toView(nextCoordinate);
      path.lineTo((int)nextPoint.getX(), (int)nextPoint.getY());
    }
    final Point2D tentativePoint = toView(tentativeCoordinate);
    path.lineTo((int)tentativePoint.getX(), (int)tentativePoint.getY());
    // close path (for rings only)
    if (closeRing) {
      path.lineTo((int)firstPoint.getX(), (int)firstPoint.getY());
    }

    drawVertices(path);

    return path;
  }

  protected boolean isFinishingRelease(final MouseEvent e) {
    return e.getClickCount() == clickCountToFinish;
  }

  public Coordinates lastCoordinate() {
    if (coordinates.size() <= 0) {
      return null;
    }
    return coordinates.get(coordinates.size() - 1);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    super.mouseDragged(e);
    mouseLocationChanged(e);
  }

  protected void mouseLocationChanged(final MouseEvent e) {
    try {
      tentativeCoordinate = toModelSnapped(e.getPoint());
      redrawIndicator();
    } catch (final Throwable t) {
    }
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    super.mouseMoved(e);
    mouseLocationChanged(e);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    try {
      super.mousePressed(e);

      // Don't add more than one point for double-clicks. A double-click
      // will
      // generate two events: one with click-count = 1 and another with
      // click-count = 2. Handle the click-count = 1 event and ignore
      // the rest.
      if (e.getClickCount() != 1) {
        return;
      }

      add(toModelSnapped(e.getPoint()));
    } catch (final Throwable t) {
      // getPanel().getContext().handleThrowable(t);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    try {
      // Can't assert that coordinates is not empty at this point
      // because
      // of the following situation: NClickTool, n=1, user
      // double-clicks.
      // Two events are generated: clickCount=1 and clickCount=2.
      // When #mouseReleased is called with the clickCount=1 event,
      // coordinates is not empty. But then #finishGesture is called and
      // the
      // coordinates are cleared. When #mouseReleased is then called
      // with
      // the clickCount=2 event, coordinates is empty!

      // Even though drawing is done in #mouseLocationChanged, call it
      // here
      // also so that #isGestureInProgress returns true on a mouse
      // click.
      // This is mainly for the benefit of OrCompositeTool, which
      // calls #isGestureInProgress.
      // Can't do this in #mouseClicked because #finishGesture may be
      // called
      // by #mouseReleased (below), which happens before #mouseClicked,
      // resulting in an IndexOutOfBoundsException in #redrawShape.
      if (e.getClickCount() == 1) {
        // A double-click will generate two events: one with
        // click-count = 1 and
        // another with click-count = 2. Handle the click-count = 1
        // event and
        // ignore the rest. Otherwise, the following problem can
        // occur:
        // -- A click-count = 1 event is generated; #redrawShape is
        // called
        // -- #isFinishingClick returns true; #finishGesture is called
        // -- #finishGesture clears the points
        // -- A click-count = 2 event is generated; #redrawShape is
        // called.
        // An IndexOutOfBoundsException is thrown because points is
        // empty.
        tentativeCoordinate = toModelSnapped(e.getPoint());
        redrawIndicator();
      }

      super.mouseReleased(e);

      // Check for finish at #mouseReleased rather than #mouseClicked.
      // #mouseReleased is a more general condition, as it applies to
      // both
      // drags and clicks.
      if (isFinishingRelease(e)) {
        finishGesture();
      }
    } catch (final Throwable t) {
    }
  }

  protected void setClickCountToFinishGesture(final int clickCountToFinish) {
    this.clickCountToFinish = clickCountToFinish;
  }

  protected void setCloseRing(final boolean closeRing) {
    this.closeRing = closeRing;
  }

  protected void setDrawBandLines(final boolean drawBandLines) {
    this.drawBandLines = drawBandLines;
  }

  protected Coordinates[] toArray(final List coordinates) {
    return (Coordinates[])coordinates.toArray(new Coordinates[] {});
  }

}
