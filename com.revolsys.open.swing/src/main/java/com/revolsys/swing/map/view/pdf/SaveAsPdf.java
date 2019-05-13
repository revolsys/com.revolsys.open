package com.revolsys.swing.map.view.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.XmpSerializer;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;

public class SaveAsPdf {

  private static void addLayerOrder(final PdfViewRenderer view, final COSArray parentLayers,
    final String title, final LayerGroup layerGroup) {
    final PDOptionalContentGroup contentGroup = view.getPDOptionalContentGroup(layerGroup);
    if (contentGroup != null) {
      final COSArray layerList = new COSArray();
      parentLayers.add(layerList);

      boolean hasLayer = false;
      layerList.add(new COSString(title));
      layerList.add(contentGroup);
      for (final Layer childLayer : layerGroup.getLayers()) {
        if (childLayer instanceof LayerGroup) {
          final LayerGroup childGroup = (LayerGroup)childLayer;
          final String childGroupName = childGroup.getName();
          addLayerOrder(view, layerList, childGroupName, childGroup);
        } else {
          final PDOptionalContentGroup childContentGroup = view
            .getPDOptionalContentGroup(childLayer);
          if (childContentGroup != null) {
            hasLayer = true;
            layerList.add(childContentGroup);
          }
        }
      }
      if (hasLayer && layerList.size() == 3) {
        layerList.remove(1);
      }
    }
  }

  private static void addOptionalContentGroup(final PdfViewRenderer view,
    final PDOptionalContentProperties optionalContentProperties, final Layer layer) {
    String layerName = layer.getName();
    if (layer instanceof LayerGroup) {
      layerName += " Child Layers";
    }
    PDOptionalContentGroup contentGroup = view.getPDOptionalContentGroup(layer);
    if (contentGroup == null) {
      contentGroup = new PDOptionalContentGroup(layerName);
      optionalContentProperties.addGroup(contentGroup);
      view.addPDOptionalContentGroup(layer, contentGroup);
    }
  }

  private static void addOptionalContentGroups(final PdfViewRenderer view,
    final double scaleForVisible, final PDOptionalContentProperties optionalContentProperties,
    final Layer layer, final boolean basemap) {
    if (layer.isVisible(scaleForVisible)) {
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        final List<Layer> layers = new ArrayList<>(layerGroup.getLayers());

        for (final Layer childLayer : layers) {
          addOptionalContentGroups(view, scaleForVisible, optionalContentProperties, childLayer,
            basemap);
        }
      } else {
        for (final Layer layerToAdd : layer.getPathList()) {
          if (layerToAdd instanceof Project) {
            if (!basemap) {
              addOptionalContentGroup(view, optionalContentProperties, layerToAdd);

            }
          } else {
            addOptionalContentGroup(view, optionalContentProperties, layerToAdd);
          }
        }
      }
    }
  }

  private static void createLayerTree(final PdfViewRenderer view, final PDDocument document,
    final Project project) {
    final PDDocumentCatalog catalog = document.getDocumentCatalog();
    PDOptionalContentProperties optionalContentProperties = catalog.getOCProperties();
    if (optionalContentProperties == null) {
      optionalContentProperties = new PDOptionalContentProperties();
      catalog.setOCProperties(optionalContentProperties);
    }
    final double scaleForVisible = view.getScaleForVisible();
    addOptionalContentGroups(view, scaleForVisible, optionalContentProperties,
      project.getBaseMapLayers(), true);
    addOptionalContentGroups(view, scaleForVisible, optionalContentProperties, project, false);

    final COSDictionary layerDict = (COSDictionary)optionalContentProperties.getCOSObject();
    final COSDictionary layerDictD = (COSDictionary)layerDict.getDictionaryObject(COSName.D);
    final COSArray layerList = (COSArray)layerDictD.getDictionaryObject(COSName.ORDER);
    layerList.clear();
    addLayerOrder(view, layerList, "Layers", project);
    addLayerOrder(view, layerList, "Base Maps", project.getBaseMapLayers());
  }

  public static void save(final File file, final Project project) {
    try {
      final PDDocument document = new PDDocument();

      final Viewport2D viewport = project.getViewport();
      BoundingBox boundingBox = viewport.getBoundingBox();
      final int width = (int)Math.ceil(viewport.getViewWidthPixels());
      final int height = (int)Math.ceil(viewport.getViewHeightPixels());

      final int srid = boundingBox.getHorizontalCoordinateSystemId();
      if (srid == 3857) {
        boundingBox = boundingBox.bboxEdit(editor -> editor
          .setGeometryFactory(viewport.getGeometryFactory().getGeographicGeometryFactory()));
      }
      final PDRectangle pageSize = new PDRectangle(width, height);
      final PDPage page = new PDPage(pageSize);

      try (
        PdfViewport pdfViewport = new PdfViewport(document, page, project, width, height,
          boundingBox)) {
        final PdfViewRenderer view = pdfViewport.newViewRenderer();
        createLayerTree(view, document, project);
        view.renderLayer(project.getBaseMapLayers());
        view.renderLayer(project);
      }
      document.addPage(page);

      final PDDocumentCatalog catalog = document.getDocumentCatalog();
      final PDMetadata metadata = new PDMetadata(document);
      catalog.setMetadata(metadata);

      // jempbox version
      final XMPMetadata xmpMetadata = XMPMetadata.createXMPMetadata();
      final DublinCoreSchema dcSchema = xmpMetadata.createAndAddDublinCoreSchema();

      dcSchema.setAboutAsSimple("");

      final XmpSerializer serializer = new XmpSerializer();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serializer.serialize(xmpMetadata, baos, false);
      metadata.importXMPMetadata(baos.toByteArray());

      document.save(file);
    } catch (final Throwable e) {
      Logs.error(SaveAsPdf.class, "Unable to create PDF " + file, e);
    }
  }
}
