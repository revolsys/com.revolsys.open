package com.revolsys.ui.html.serializer.key;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.JavaBeanUtil;

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
  public void serialize(final XmlWriter out, final Object object) {
    final Object property = JavaBeanUtil.getProperty(object, getName());
    if (property == null) {
      out.text("-");
    } else if (property instanceof Map) {
      final Map<Object, Object> map = (Map)property;
      if (map.isEmpty()) {
        out.text("-");
      } else {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, "objectList");

        out.startTag(HtmlUtil.TABLE);
        out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
        out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");
        out.attribute(HtmlUtil.ATTR_CLASS, "data");
        out.startTag(HtmlUtil.THEAD);
        out.startTag(HtmlUtil.TR);

        out.startTag(HtmlUtil.TH);
        out.attribute(HtmlUtil.ATTR_CLASS, "firstCol");
        out.text(keyLabel);
        out.endTag(HtmlUtil.TH);

        out.startTag(HtmlUtil.TH);
        out.attribute(HtmlUtil.ATTR_CLASS, "lastCol");
        out.text(valueLabel);
        out.endTag(HtmlUtil.TH);

        out.endTag(HtmlUtil.TR);
        out.endTag(HtmlUtil.THEAD);

        out.startTag(HtmlUtil.TBODY);
        boolean odd = true;
        boolean first = true;
        for (final Iterator<Entry<Object, Object>> entries = map.entrySet()
          .iterator(); entries.hasNext();) {
          final Entry<Object, Object> entry = entries.next();
          out.startTag(HtmlUtil.TR);
          String cssClass = "";
          if (first) {
            cssClass = "firstRow ";
            first = false;
          }
          if (!entries.hasNext()) {
            cssClass = "lastRow ";
          }
          if (odd) {
            cssClass += "odd";
          } else {
            cssClass += "even";
          }
          out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
          odd = !odd;
          final Object key = entry.getKey();
          String keyText = "-";
          if (key != null) {
            keyText = key.toString();
            if (!StringUtils.hasText(keyText)) {
              keyText = "-";
            }
          }
          out.startTag(HtmlUtil.TD);
          out.attribute(HtmlUtil.ATTR_CLASS, "firstCol");
          out.text(keyText);
          out.endTag(HtmlUtil.TD);

          final Object value = entry.getValue();
          String valueText = "-";
          if (value != null) {
            valueText = value.toString();
            if (!StringUtils.hasText(valueText)) {
              valueText = "-";
            }
          }
          out.startTag(HtmlUtil.TD);
          out.attribute(HtmlUtil.ATTR_CLASS, "lastCol");
          out.text(valueText);
          out.endTag(HtmlUtil.TD);

          out.endTag(HtmlUtil.TR);
        }
        out.endTag(HtmlUtil.TBODY);
        out.endTag(HtmlUtil.TABLE);
        out.endTag(HtmlUtil.DIV);
      }
    } else {
      out.text(property.toString());
    }
  }

  public void setKeyLabel(final String keyLabel) {
    this.keyLabel = keyLabel;
  }

  public void setValueLabel(final String valueLabel) {
    this.valueLabel = valueLabel;
  }
}
