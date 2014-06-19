package com.revolsys.swing.pdf;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;

public class SaveAsPdf {
  private static void render(final PDPage page, final BoundingBox boundingBox,
    final Layer layer) {
    if (layer.isVisible()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        for (final Layer childLayer : layerGroup) {
          render(page, boundingBox, childLayer);
        }
      } else if (layer instanceof AbstractDataObjectLayer) {
        final AbstractDataObjectLayer dataObjectLayer = (AbstractDataObjectLayer)layer;

      }
    }
  }

  public static void save() {
    try {
      final PDDocument document = new PDDocument();

      final Project project = Project.get();
      final BoundingBox boundingBox = project.getViewBoundingBox();
      final double aspectRatio = boundingBox.getAspectRatio();
      final PDPage page = new PDPage(PDPage.PAGE_SIZE_LETTER);
      if (aspectRatio > 1) {
        page.setRotation(90);
      }
      render(page, boundingBox, project);
      document.addPage(page);

      document.save(new File("/Users/paustin/Downloads/map.pdf"));
    } catch (final Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
