package com.revolsys.swing.builder;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectUiBuilder extends AbstractUiBuilder {
  private static final String TABLE_STYLE = "width: 100%;font-family: Tahoma, Arial, sans-serif;font-size: 10pt;padding: 0px;border-collapse: collapse;";

  private static final String TABLE_ATTRS = "style=\"" + TABLE_STYLE + "\" ";

  private static final String TABLE_HEADING_STYLE = "vertical-align: top;text-align: left;padding: 1px 2px;width: auto;";

  private static final String TABLE_HEADING_ODD_ATTRS = "style=\"background-color: #FFFFFF;"
    + TABLE_HEADING_STYLE + "\"";

  private static final String TABLE_HEADING_EVEN_ATTRS = "style=\"background-color: #E6E6E6;"
    + TABLE_HEADING_STYLE + "\"";

  private static final String TABLE_CELL_STYLE = "vertical-align: top;text-align: left;border-left: 1px solid black;padding: 0px;";

  private static final String TABLE_CELL_ODD_ATTRS = "style=\"background-color: #FFFFFF;"
    + TABLE_CELL_STYLE + "\"";

  private static final String TABLE_CELL_EVEN_ATTRS = "style=\"background-color: #E6E6E6;"
    + TABLE_CELL_STYLE + "\"";

  private static final String TYPE_TITLE_ATTRS = "style=\"font-size: 12pt;font-weight:bold;padding: 1px 2px;background-color:#FFFFCC\"";

  public DataObjectUiBuilder() {
  }

  private void appendEscaped(final StringBuffer s, final String value) {
    s.append(escapeHTML(value, false, false));
  }

  @Override
  public void appendHtml(final StringBuffer s, final Object object) {
    appendHtml(s, object, true);
  }

  public void appendHtml(
    final StringBuffer s,
    final Object object,
    final boolean nested) {
    if (object instanceof DataObject) {
      final DataObject feature = (DataObject)object;
      final DataObjectMetaData schema = feature.getMetaData();
      if (nested) {
        final String schemaName = schema.getPath();
        if (schemaName != null) {
          appendValue(s, schemaName, TYPE_TITLE_ATTRS);
        }
      }
      s.append("<table ").append(TABLE_ATTRS).append(">\n");
      boolean odd = true;
      for (int i = 0; i < schema.getAttributeCount(); i++) {
        final Object value = feature.getValue(i);
        if (value instanceof Geometry) {
          continue;
        }
        final String name = schema.getAttributeName(i);
        appendRow(s, odd, name, value);
        odd = !odd;
      }
      s.append("</table>\n");
    }
  }

  private void appendRow(
    final StringBuffer s,
    final boolean odd,
    final String name,
    final Object value) {
    if (odd) {
      s.append("<tr valign=\"top\" class=\"odd\"><th ")
        .append(TABLE_HEADING_ODD_ATTRS)
        .append(">");
    } else {
      s.append("<tr valign=\"top\" class=\"even\"><th ")
        .append(TABLE_HEADING_EVEN_ATTRS)
        .append(">");
    }
    appendEscaped(s, name);
    s.append("</th>\n");
    if (odd) {
      s.append("<td ").append(TABLE_CELL_ODD_ATTRS).append(">");
    } else {
      s.append("<td ").append(TABLE_CELL_EVEN_ATTRS).append(">");
    }
    appendValue(s, value);
    s.append("</td></tr>\n");
  }

  private void appendValue(final StringBuffer s, final Object value) {
    s.append("<div>");
    if (value == null) {
      s.append("-");
    } else {
      final String string = getRegistry().toHtml(value);
      s.append(string);
    }
    s.append("</div>");
  }

  private void appendValue(
    final StringBuffer s,
    final String value,
    final String style) {
    s.append("<div ").append(style).append(">");
    if (value == null) {
      s.append("-");
    } else {
      appendEscaped(s, value);
    }
    s.append("</div>");
  }

}
