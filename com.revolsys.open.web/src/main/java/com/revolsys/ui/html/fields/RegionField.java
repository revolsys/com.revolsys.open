package com.revolsys.ui.html.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.domain.Region;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class RegionField extends Field {

  private String countryCode;

  private List<Region> regions = new ArrayList<>();

  private String stringValue;

  public RegionField(final String name, final boolean required) {
    super(name, required);
  }

  @Override
  public boolean hasValue() {
    return this.stringValue != null && !this.stringValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.stringValue = request.getParameter(getName());
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      if (this.regions.size() > 0) {
        final Region region = Region.getRegionByName(this.countryCode, this.stringValue);
        if (region == null) {
          addValidationError("Invalid Value");
          valid = false;
        } else {
          setValue(region.getName());
        }
      } else {
        setValue(this.stringValue);
      }
    }
    return valid;
  }

  @Override
  public void postInit(final HttpServletRequest request) {
    final CountryField countryField = (CountryField)getForm().getField("country");
    this.countryCode = countryField.getCountryCode();
    if (this.countryCode != null) {
      this.regions = Region.getRegions(this.countryCode);
    }
    if (this.stringValue == null) {
      setValue(getInitialValue(request));
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.regions.size() > 0) {
      serializeSelectField(out);
    } else {
      serializeTextField(out);
    }
  }

  private void serializeOptions(final XmlWriter out) {
    for (final Region region : this.regions) {
      out.startTag(HtmlElem.OPTION);
      if (region.getName().equals(this.stringValue)) {
        out.attribute(HtmlAttr.SELECTED, "true");
      }
      out.text(region.getName());
      out.endTag(HtmlElem.OPTION);
    }
  }

  /**
   * @param out
   * @throws IOException
   */
  private void serializeSelectField(final XmlWriter out) {
    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.NAME, getName());
    serializeOptions(out);
    out.endTag(HtmlElem.SELECT);
  }

  private void serializeTextField(final XmlWriter out) {
    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.TYPE, "text");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    out.attribute(HtmlAttr.SIZE, "30");
    out.attribute(HtmlAttr.MAX_LENGTH, "30");
    if (this.stringValue != null) {
      out.attribute(HtmlAttr.VALUE, this.stringValue);
    }
    out.endTag(HtmlElem.INPUT);
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    this.stringValue = null;
    if (this.regions.size() > 0) {
      if (value != null) {
        Region region = Region.getRegionByName(this.countryCode, (String)value);
        if (region == null) {
          region = Region.getRegionByCode(this.countryCode, (String)value);
        }
        if (region != null) {
          this.stringValue = region.getName();
        }
      }
    }
  }
}
