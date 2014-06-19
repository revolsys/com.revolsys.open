package com.revolsys.swing.pdf;

import org.apache.pdfbox.pdmodel.PDPage;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;

public class PdfViewport extends Viewport2D {

  public PdfViewport(final PDPage page, final Project project, final int width,
    final int height, final BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
  }

}
