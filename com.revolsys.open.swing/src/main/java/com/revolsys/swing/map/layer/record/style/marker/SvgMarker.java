package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.Icon;

import org.apache.batik.transcoder.TranscoderInput;
import org.jeometry.common.logging.Logs;
import org.w3c.dom.Document;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.BaseCloseable;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.util.CaseConverter;

public class SvgMarker extends AbstractMarker {

  private WeakReference<Icon> icon = new WeakReference<>(null);

  private WeakReference<TranscoderInput> transcoderInput = new WeakReference<>(null);

  public SvgMarker(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public SvgMarker(final String name) {
    this(name, CaseConverter.toCapitalizedWords(name));
  }

  public SvgMarker(final String name, final String title) {
    super(name, title);
  }

  private synchronized TranscoderInput getTranscoderInput() {
    TranscoderInput input = this.transcoderInput.get();
    if (input == null) {
      final Resource resource = new ClassPathResource(getName() + ".svg");
      try {
        final String uri = resource.getUriString();
        final Document document = SvgUtil.newDocument(uri);
        input = new TranscoderInput(document);
        input.setURI(uri);
      } catch (final Throwable e) {
        Logs.error(this, "Cannot open :" + resource, e);
      }
      this.transcoderInput = new WeakReference<>(input);
    }
    return input;
  }

  @Override
  public String getTypeName() {
    return "markerSvg";
  }

  @Override
  public boolean isUseMarkerName() {
    return true;
  }

  @Override
  public synchronized Icon newIcon(final MarkerStyle style) {
    Icon icon = this.icon.get();
    if (icon == null) {
      final Resource resource = new ClassPathResource(getName() + ".svg");
      try {
        final String uri = resource.getUriString();
        final Document document = SvgUtil.newDocument(uri);
        icon = new SvgIcon(document, 16, 16);
      } catch (final Throwable e) {
        Logs.error(this, "Cannot open :" + resource, e);
      }
      this.icon = new WeakReference<Icon>(icon);
    }

    return icon;
  }

  @Override
  public void render(final Graphics2DViewRenderer view, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY, double orientation) {
    final TranscoderInput transcoderInput = getTranscoderInput();
    if (transcoderInput != null) {
      try (
        BaseCloseable closable = view.useViewCoordinates()) {
        final Quantity<Length> markerWidth = style.getMarkerWidth();
        final double mapWidth = view.toDisplayValue(markerWidth);
        final Quantity<Length> markerHeight = style.getMarkerHeight();
        final double mapHeight = view.toDisplayValue(markerHeight);
        final String orientationType = style.getMarkerOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        }

        view.translateModelToViewCoordinates(modelX, modelY);

        final double markerOrientation = style.getMarkerOrientation();
        orientation = orientation + markerOrientation;
        if (orientation != 0) {
          graphics.rotate(Math.toRadians(orientation));
        }

        final Quantity<Length> deltaX = style.getMarkerDx();
        final Quantity<Length> deltaY = style.getMarkerDy();
        double dx = view.toDisplayValue(deltaX);
        double dy = view.toDisplayValue(deltaY);

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
        Logs.error(this, "Unable to render", e);
      }
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();

    return map;
  }
}
