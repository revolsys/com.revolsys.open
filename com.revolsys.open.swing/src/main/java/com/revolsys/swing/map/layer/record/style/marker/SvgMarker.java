package com.revolsys.swing.map.layer.record.style.marker;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.swing.Icon;

import org.apache.batik.transcoder.TranscoderException;
import org.jeometry.common.logging.Logs;
import org.w3c.dom.Document;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.CaseConverter;

public class SvgMarker extends AbstractMarker {

  private Reference<Icon> icon = new WeakReference<>(null);

  private Reference<Document> document = new WeakReference<>(null);

  private boolean hasError = false;

  private String uri;

  public SvgMarker(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public SvgMarker(final String name) {
    this(name, CaseConverter.toCapitalizedWords(name));
  }

  public SvgMarker(final String name, final String title) {
    super(name, title);
  }

  public synchronized Document getDocument() {
    Document document = this.document.get();
    if (document == null) {
      if (!this.hasError) {
        final String uri = getUri();
        try {
          document = SvgUtil.newDocument(uri);
          if (document != null) {
            this.document = new WeakReference<>(document);
          }
        } catch (final Throwable e) {
          this.hasError = true;
          Logs.error(this, "Cannot open svg:" + uri, e);
        }
      }
    }
    return document;
  }

  @Override
  public String getTypeName() {
    return "markerSvg";
  }

  public String getUri() {
    if (this.uri == null) {
      final Resource resource = new ClassPathResource(getName() + ".svg");
      this.uri = resource.getUriString();
    }
    return this.uri;
  }

  @Override
  public boolean isUseMarkerName() {
    return true;
  }

  @Override
  public synchronized Icon newIcon(final MarkerStyle style) {
    Icon icon = this.icon.get();
    if (icon == null) {
      try {
        final Document document = getDocument();
        icon = new SvgIcon(document, 16, 16);
        this.icon = new WeakReference<>(icon);
      } catch (final TranscoderException e) {
      }
    }

    return icon;
  }

  @Override
  public MarkerRenderer newMarkerRenderer(final ViewRenderer view, final MarkerStyle style) {
    return view.newMarkerRendererSvg(this, style);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();

    return map;
  }
}
