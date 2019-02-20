package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.Icon;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;
import com.revolsys.util.Property;

public abstract class AbstractMarker extends BaseObjectWithPropertiesAndChange implements Marker {
  public static void translateMarker(final Graphics2DViewRender view, final Graphics2D graphics,
    final MarkerStyle style, final double x, final double y, final double width,
    final double height, double orientation) {
    view.translateModelToViewCoordinates(x, y);
    final double markerOrientation = style.getMarkerOrientation();
    orientation = -orientation + markerOrientation;
    if (orientation != 0) {
      graphics.rotate(Math.toRadians(orientation));
    }

    final Quantity<Length> deltaX = style.getMarkerDx();
    final Quantity<Length> deltaY = style.getMarkerDy();
    double dx = view.toDisplayValue(deltaX);
    double dy = view.toDisplayValue(deltaY);
    final String verticalAlignment = style.getMarkerVerticalAlignment();
    if ("bottom".equals(verticalAlignment)) {
      dy -= height;
    } else if ("auto".equals(verticalAlignment) || "middle".equals(verticalAlignment)) {
      dy -= height / 2;
    }
    final String horizontalAlignment = style.getMarkerHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      dx -= width;
    } else if ("auto".equals(horizontalAlignment) || "center".equals(horizontalAlignment)) {
      dx -= width / 2;
    }
    graphics.translate(dx, dy);
  }

  private String markerType;

  public AbstractMarker() {
  }

  public AbstractMarker(final Map<String, Object> properties) {
    setProperties(properties);
  }

  public AbstractMarker(final String markerType) {
    setMarkerType(markerType);
  }

  @Override
  public String getMarkerType() {
    if (Property.hasValue(this.markerType)) {
      return this.markerType;
    } else {
      return "unknown";
    }
  }

  @Override
  public int hashCode() {
    final String markerType = getMarkerType();
    if (Property.hasValue(markerType)) {
      return markerType.hashCode();
    } else {
      return super.hashCode();
    }
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {
    return null;
  }

  protected void postSetMarkerType() {
  }

  public void setMarkerType(final String markerType) {
    final Object oldValue = this.markerType;
    if (!DataType.equal(markerType, oldValue)) {
      this.markerType = markerType;
      postSetMarkerType();
      firePropertyChange("name", oldValue, this.markerType);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    return map;
  }

  @Override
  public String toString() {
    final String markerType = getMarkerType();
    if (Property.hasValue(markerType)) {
      return markerType;
    } else {
      return super.toString();
    }
  }
}
