package com.revolsys.ui.html.serializer.key;

import java.io.Reader;
import java.sql.Clob;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.logging.Logs;

import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class MapTableKeySerializer extends AbstractKeySerializer {

  private String keyLabel;

  private String valueLabel;

  public MapTableKeySerializer() {
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  public MapTableKeySerializer(final String name, final String label) {
    super(name, label);
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
      if (value instanceof Clob) {
        final Clob clob = (Clob)value;
        try (
          Reader reader = clob.getCharacterStream()) {
          value = Json.toMap(reader);
        } catch (final Throwable e) {
          Logs.error(this, "Unable to read from clob", e);
        }
      } else if (value instanceof String) {
        final String string = (String)value;
        if (Property.hasValue(string)) {
          try {
            value = Json.toMap(string);
          } catch (final Throwable e) {
          }
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
          out.startTag(HtmlElem.DIV);
          out.attribute(HtmlAttr.CLASS, "panel panel-default table-responsive batchJob");

          out.startTag(HtmlElem.TABLE);
          out.attribute(HtmlAttr.CELL_SPACING, "0");
          out.attribute(HtmlAttr.CELL_PADDING, "0");
          out.attribute(HtmlAttr.CLASS, "table table-striped table-condensed");
          out.startTag(HtmlElem.THEAD);
          out.startTag(HtmlElem.TR);

          out.startTag(HtmlElem.TH);
          out.text(this.keyLabel);
          out.endTag(HtmlElem.TH);

          out.startTag(HtmlElem.TH);
          out.text(this.valueLabel);
          out.endTag(HtmlElem.TH);

          out.endTag(HtmlElem.TR);
          out.endTag(HtmlElem.THEAD);

          out.startTag(HtmlElem.TBODY);
          for (final Entry<Object, Object> entry : map.entrySet()) {
            out.startTag(HtmlElem.TR);
            final Object key = entry.getKey();
            String keyText = "-";
            if (key != null) {
              keyText = key.toString();
              if (!Property.hasValue(keyText)) {
                keyText = "-";
              }
            }
            out.startTag(HtmlElem.TD);
            out.text(keyText);
            out.endTag(HtmlElem.TD);

            final Object entryValue = entry.getValue();
            String valueText = "-";
            if (entryValue != null) {
              valueText = entryValue.toString();
              if (!Property.hasValue(valueText)) {
                valueText = "-";
              }
            }
            out.startTag(HtmlElem.TD);
            out.text(valueText);
            out.endTag(HtmlElem.TD);

            out.endTag(HtmlElem.TR);
          }
          out.endTag(HtmlElem.TBODY);
          out.endTag(HtmlElem.TABLE);
          out.endTag(HtmlElem.DIV);
        }
      } else {
        out.text(value.toString());
      }
    }
  }

  public MapTableKeySerializer setKeyLabel(final String keyLabel) {
    this.keyLabel = keyLabel;
    return this;
  }

  public MapTableKeySerializer setValueLabel(final String valueLabel) {
    this.valueLabel = valueLabel;
    return this;
  }
}
