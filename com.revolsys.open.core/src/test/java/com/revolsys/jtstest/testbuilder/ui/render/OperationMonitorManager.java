package com.revolsys.jtstest.testbuilder.ui.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jtstest.testbuilder.Viewport;

public class OperationMonitorManager {
  public static Geometry indicator = null;

  // testing only
  static {
    final GeometryFactory geomFact = GeometryFactory.getFactory();
    indicator = geomFact.lineString(new Coordinates[] {
      new Coordinate(0, 0), new Coordinate(100, 10)
    });
  }

  private final JPanel panel;

  private final Viewport viewport;

  private final Timer repaintTimer = new Timer(50, new ActionListener() {
    @Override
    public void actionPerformed(final ActionEvent e) {
      if (indicator != null) {
        paint();
        return;
      }
    }
  });

  public OperationMonitorManager(final JPanel panel, final Viewport viewport) {
    this.panel = panel;
    this.viewport = viewport;
    // start with a short time cycle to give better appearance
    repaintTimer.setInitialDelay(1000);
    repaintTimer.start();
  }

  private void paint() {
    final Graphics2D g = (Graphics2D)panel.getGraphics();
    if (g == null) {
      return;
    }
    GeometryPainter.paint(indicator, viewport, g, Color.RED, null);
  }

}
