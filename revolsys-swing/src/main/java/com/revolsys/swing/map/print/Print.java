package com.revolsys.swing.map.print;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.Invoke;

public class Print extends I18nAction {
  private static final Icon ICON = Icons.getIcon("printer");

  /**
   *
   */
  private static final long serialVersionUID = 8194892040166851551L;

  private PrintService printService;

  public Print() {
    super(ICON);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Project project = Project.get();
    final Viewport2D viewport = project.getViewport();

    final PrinterJob job = PrinterJob.getPrinterJob();

    final PageFormat format = job.defaultPage();
    format.setOrientation(PageFormat.PORTRAIT);
    final Paper paper = format.getPaper();
    paper.setImageableArea(29, 29, format.getWidth() - 58, format.getHeight() - 58);
    format.setPaper(paper);
    if (this.printService != null) {
      try {
        job.setPrintService(this.printService);
      } catch (final PrinterException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    final BoundingBox boundingBox = viewport.getBoundingBox();
    final MapPageable pageable = new MapPageable(project, boundingBox, format, 20000, 300, 200);
    job.setPageable(pageable);
    final boolean doPrint = job.printDialog();
    if (doPrint) {
      this.printService = job.getPrintService();
      Invoke.background("Print", () -> {
        try {
          job.print();
        } catch (final Exception e) {
          Logs.error(this, "Unable to print", e);
        }
      });
    }

  }
}
