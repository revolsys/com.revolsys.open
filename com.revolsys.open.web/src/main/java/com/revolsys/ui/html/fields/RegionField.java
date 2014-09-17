package com.revolsys.ui.html.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.ui.html.domain.Region;
import com.revolsys.ui.html.form.Form;

public class RegionField extends Field {

  private String stringValue;

  private List<Region> regions = new ArrayList<Region>();

  private String countryCode;

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
        final Region region = Region.getRegionByName(this.countryCode,
          this.stringValue);
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
    final CountryField countryField = (CountryField)getForm().getField(
        "country");
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
      out.startTag(HtmlUtil.OPTION);
      if (region.getName().equals(this.stringValue)) {
        out.attribute(HtmlUtil.ATTR_SELECTED, "true");
      }
      out.text(region.getName());
      out.endTag(HtmlUtil.OPTION);
    }
  }

  /**
   * @param out
   * @throws IOException
   */
  private void serializeSelectField(final XmlWriter out) {
    out.startTag(HtmlUtil.SELECT);
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    serializeOptions(out);
    out.endTag(HtmlUtil.SELECT);
  }

  private void serializeTextField(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    out.attribute(HtmlUtil.ATTR_SIZE, "30");
    out.attribute(HtmlUtil.ATTR_MAX_LENGTH, "30");
    if (this.stringValue != null) {
      out.attribute(HtmlUtil.ATTR_VALUE, this.stringValue);
    }
    out.endTag(HtmlUtil.INPUT);
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
