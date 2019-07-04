package com.revolsys.swing.map.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.ViewportCacheBoundingBox;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewport;
import com.revolsys.swing.parallel.Invoke;

public class SinglePage extends Graphics2DViewport implements Pageable, Printable {

  public static void print() {
    final Project project = Project.get();
    final Viewport2D viewport = project.getViewport();
    final ViewportCacheBoundingBox cacheBoundingBox = viewport.getCacheBoundingBox();
    final BoundingBox boundingBox = cacheBoundingBox.getBoundingBox();
    final int viewWidth = cacheBoundingBox.getViewWidthPixels();
    final int viewHeight = cacheBoundingBox.getViewHeightPixels();
    final double scaleForVisible = cacheBoundingBox.getScale();

    final PrinterJob job = PrinterJob.getPrinterJob();

    job.setJobName(project.getName());
    final PageFormat format = job.defaultPage();
    final PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();

    if (boundingBox.getAspectRatio() > 1) {
      format.setOrientation(PageFormat.LANDSCAPE);
      // printAttributes.add(OrientationRequested.LANDSCAPE);
    } else {
      format.setOrientation(PageFormat.PORTRAIT);
      // printAttributes.add(OrientationRequested.PORTRAIT);
    }

    final SinglePage pageable = new SinglePage(project, boundingBox, viewWidth, viewHeight,
      scaleForVisible);
    job.setPageable(pageable);

    final boolean doPrint = job.printDialog();
    if (doPrint) {
      Invoke.background("Print", () -> {
        try {
          job.print();
        } catch (final PrinterAbortException e) {
        } catch (final Exception e) {
          Logs.error(SinglePage.class, "Unable to print", e);
        }
      });
    }
  }

  private final double scaleForVisible;

  public SinglePage(final Project project, final BoundingBox boundingBox, final double viewWidth,
    final double viewHeight, final double scaleForVisible) {
    super(project, viewWidth, viewHeight, boundingBox);
    this.scaleForVisible = scaleForVisible;
  }

  @Override
  public double getMetresPerPixel() {
    return 2.54e-3 / 72;
  }

  @Override
  public int getNumberOfPages() {
    return 1;
  }

  @Override
  public PageFormat getPageFormat(final int pageIndex) {
    final PageFormat pageFormat = new PageFormat();
    final Paper paper = new Paper();
    final double viewWidth = getViewWidthPixels();
    final double viewHeight = getViewHeightPixels();
    double width;
    double height;
    if (viewWidth > viewHeight) {
      width = viewHeight;
      height = viewWidth;
      pageFormat.setOrientation(PageFormat.LANDSCAPE);
    } else {
      width = viewWidth;
      height = viewHeight;
    }
    paper.setSize(width, height);
    paper.setImageableArea(0, 0, width, height);
    pageFormat.setPaper(paper);
    return pageFormat;
  }

  @Override
  public Printable getPrintable(final int pageIndex) {
    if (pageIndex == 0) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public Graphics2DViewRenderer newViewRenderer(final Graphics graphics) {
    final Graphics2DViewRenderer renderer = new Graphics2DViewRenderer(this, (Graphics2D)graphics);
    renderer.setShowHiddenRecords(true);
    return renderer;
  }

  @Override
  public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
    throws PrinterException {
    if (pageIndex == 0) {
      final Graphics2DViewRenderer view = newViewRenderer(graphics);
      view.setScaleForVisible(this.scaleForVisible);
      final int translateX = (int)pageFormat.getImageableX();
      final int translateY = (int)pageFormat.getImageableY();
      graphics.translate(translateX - 1, translateY - 1);
      final Project project = getProject();
      final MapPanel mapPanel = project.getMapPanel();
      final Layer baseMapLayer = mapPanel.getBaseMapLayer();
      view.renderLayer(baseMapLayer);

      view.renderLayer(project);
      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
  }

}
