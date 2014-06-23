package com.revolsys.swing.pdf;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;

public class SaveAsPdf {

  public static void save() {
    try {
      final PDDocument document = new PDDocument();

      final Project project = Project.get();
      final Viewport2D viewport = MapPanel.get(project).getViewport();
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final int width = viewport.getViewWidthPixels();
      final int height = viewport.getViewHeightPixels();
      final PDRectangle pageSize = new PDRectangle(width, height);
      final PDPage page = new PDPage(pageSize);
      try (
        PdfViewport pdfViewport = new PdfViewport(document, page, project,
          width, height, boundingBox)) {
        final LayerRenderer<? extends Layer> renderer = project.getRenderer();
        renderer.render(pdfViewport);
      }
      document.addPage(page);

      document.save(new File("/Users/paustin/Downloads/map.pdf"));
    } catch (final Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
