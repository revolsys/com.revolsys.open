package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.swing.Icon;

import org.apache.batik.transcoder.TranscoderInput;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.revolsys.awt.CloseableAffineTransform;
import com.revolsys.io.BaseCloseable;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.symbol.Symbol;
import com.revolsys.swing.map.symbol.SymbolLibrary;
import com.revolsys.util.Exceptions;

public class SvgMarker extends AbstractMarker {

  private Icon icon;

  private Document document;

  private TranscoderInput transcoderInput;

  private Symbol symbol;

  public SvgMarker() {
  }

  // protected void changeAttribute(final SVGElement element, final String
  // attrName,
  // final String oldValue, final String value) throws SVGException {
  // if (element.hasAttribute(attrName, AnimationElement.AT_CSS)) {
  // final StyleAttribute style = new StyleAttribute(attrName);
  // if (element.getStyle(style, false)) {
  // final String currentValue = style.getStringValue();
  // if (currentValue.equalsIgnoreCase(oldValue)) {
  // element.setAttribute(attrName, AnimationElement.AT_CSS, value);
  // }
  // }
  // }
  // for (int i = 0; i < element.getNumChildren(); i++) {
  // final SVGElement child = element.getChild(i);
  // changeAttribute(child, attrName, oldValue, value);
  // }
  // }

  public SvgMarker(final Map<String, Object> properties) {
    setProperties(properties);
  }

  public SvgMarker(final String markerType) {
    super(markerType);
  }

  @Override
  public boolean isUseMarkerType() {
    return true;
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {
    return this.icon;
  }

  @Override
  protected void postSetMarkerType() {
    final String markerType = getMarkerType();
    this.symbol = SymbolLibrary.findSymbol(markerType);
    final Resource resource = new ClassPathResource(markerType + ".svg");
    try {
      final String uri = resource.getUriString();
      this.document = SvgUtil.newDocument(uri);
      this.transcoderInput = new TranscoderInput(this.document);
      this.transcoderInput.setURI(uri);
      this.icon = new SvgIcon(this.document, 16, 16);
    } catch (final Throwable e) {
      this.document = null;
      Exceptions.log(getClass(), "Cannot open :" + resource, e);
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics, final MarkerStyle style,
    final double modelX, final double modelY, double orientation) {
    final TranscoderInput transcoderInput = this.transcoderInput;
    if (transcoderInput != null) {
      try (
        BaseCloseable transformCloseable = new CloseableAffineTransform(graphics)) {
        Viewport2D.setUseModelCoordinates(viewport, graphics, false);

        final Measure<Length> markerWidth = style.getMarkerWidth();
        final double mapWidth = Viewport2D.toDisplayValue(viewport, markerWidth);
        final Measure<Length> markerHeight = style.getMarkerHeight();
        final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);
        final String orientationType = style.getMarkerOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        }

        Viewport2D.translateModelToViewCoordinates(viewport, graphics, modelX, modelY);

        final double markerOrientation = style.getMarkerOrientation();
        orientation = orientation + markerOrientation;
        if (orientation != 0) {
          graphics.rotate(Math.toRadians(orientation));
        }

        final Measure<Length> deltaX = style.getMarkerDx();
        final Measure<Length> deltaY = style.getMarkerDy();
        double dx = Viewport2D.toDisplayValue(viewport, deltaX);
        double dy = Viewport2D.toDisplayValue(viewport, deltaY);

        final String verticalAlignment = style.getMarkerVerticalAlignment();
        if ("bottom".equals(verticalAlignment)) {
          dy -= mapHeight;
        } else if ("middle".equals(verticalAlignment)) {
          dy -= mapHeight / 2;
        }
        final String horizontalAlignment = style.getMarkerHorizontalAlignment();
        if ("right".equals(horizontalAlignment)) {
          dx -= mapWidth;
        } else if ("center".equals(horizontalAlignment)) {
          dx -= mapWidth / 2;
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.translate(dx, dy);

        // changeAttribute(root, "stroke", "#ffffff",
        // WebColors.toHex(style.getMarkerLineColor()));
        // changeAttribute(root, "color", "#ffffff",
        // WebColors.toHex(style.getMarkerLineColor()));
        // changeAttribute(root, "fill", "#ffffff",
        // WebColors.toHex(style.getMarkerLineColor()));
        //
        // changeAttribute(root, "stroke", "#000000",
        // WebColors.toHex(style.getMarkerFill()));
        // changeAttribute(root, "color", "#000000",
        // WebColors.toHex(style.getMarkerFill()));
        // changeAttribute(root, "fill", "#000000",
        // WebColors.toHex(style.getMarkerFill()));
        // changeAttribute(root, "stroke", "#444444",
        // WebColors.toHex(style.getMarkerFill()));
        // changeAttribute(root, "color", "#444444",
        // WebColors.toHex(style.getMarkerFill()));
        // changeAttribute(root, "fill", "#444444",
        // WebColors.toHex(style.getMarkerFill()));
        // shape.render(graphics);
        final Graphics2DTranscoder transcoder = new Graphics2DTranscoder(graphics);
        synchronized (transcoderInput) {
          transcoder.transcode(transcoderInput, null);
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to render", e);
      }
    }
  }

  @Override
  public String toString() {
    if (this.symbol == null) {
      return super.toString();
    } else {
      return this.symbol.getTitle();
    }
  }
}
