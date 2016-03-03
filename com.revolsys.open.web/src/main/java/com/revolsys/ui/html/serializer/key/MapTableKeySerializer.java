package com.revolsys.ui.html.serializer.key;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class MapTableKeySerializer extends AbstractKeySerializer {

  private String keyLabel;

  private String valueLabel;

  public MapTableKeySerializer() {
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    Object value = Property.get(object, getKey());
    if (value == null) {
      out.text("-");
    } else {
      if (value instanceof String) {
        final String string = (String)value;
        if (Property.hasValue(string)) {
          value = Json.toMap(string);
        } else {
          out.text("-");
          return;
        }
      }
      if (value instanceof Map) {
        @SuppressWarnings({
          "unchecked", "rawtypes"
        })
        final Map<Object, Object> map = (Map)value;
        if (map.isEmpty()) {
          out.text("-");
        } else {
          out.startTag(HtmlUtil.DIV);
          out.attribute(HtmlUtil.ATTR_CLASS, "panel panel-default table-responsive batchJob");

          out.startTag(HtmlUtil.TABLE);
          out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
          out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");
          out.attribute(HtmlUtil.ATTR_CLASS, "table table-striped table-condensed");
          out.startTag(HtmlUtil.THEAD);
          out.startTag(HtmlUtil.TR);

          out.startTag(HtmlUtil.TH);
          out.text(this.keyLabel);
          out.endTag(HtmlUtil.TH);

          out.startTag(HtmlUtil.TH);
          out.text(this.valueLabel);
          out.endTag(HtmlUtil.TH);

          out.endTag(HtmlUtil.TR);
          out.endTag(HtmlUtil.THEAD);

          out.startTag(HtmlUtil.TBODY);
          for (final Iterator<Entry<Object, Object>> entries = map.entrySet().iterator(); entries
            .hasNext();) {
            final Entry<Object, Object> entry = entries.next();
            out.startTag(HtmlUtil.TR);
            final Object key = entry.getKey();
            String keyText = "-";
            if (key != null) {
              keyText = key.toString();
              if (!Property.hasValue(keyText)) {
                keyText = "-";
              }
            }
            out.startTag(HtmlUtil.TD);
            out.text(keyText);
            out.endTag(HtmlUtil.TD);

            final Object entryValue = entry.getValue();
            String valueText = "-";
            if (entryValue != null) {
              valueText = entryValue.toString();
              if (!Property.hasValue(valueText)) {
                valueText = "-";
              }
            }
            out.startTag(HtmlUtil.TD);
            out.text(valueText);
            out.endTag(HtmlUtil.TD);

            out.endTag(HtmlUtil.TR);
          }
          out.endTag(HtmlUtil.TBODY);
          out.endTag(HtmlUtil.TABLE);
          out.endTag(HtmlUtil.DIV);
        }
      } else {
        out.text(value.toString());
      }
    }
  }

  public void setKeyLabel(final String keyLabel) {
    this.keyLabel = keyLabel;
  }

  public void setValueLabel(final String valueLabel) {
    this.valueLabel = valueLabel;
  }
}
