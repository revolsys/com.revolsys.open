/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.fields;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.domain.Region;
import com.revolsys.ui.html.form.Form;

/**
 * @author Paul Austin
 * @version 1.0
 */

public class RegionField extends Field {

  private static final Logger log = Logger.getLogger(RegionField.class);

  private String stringValue;

  private List<Region> regions = new ArrayList<Region>();

  private String countryCode;

  /**
   * @param name
   * @param required
   */
  public RegionField(final String name, final boolean required) {
    super(name, required);
  }

  @Override
  public boolean hasValue() {
    return stringValue != null && !stringValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    stringValue = request.getParameter(getName());
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      if (regions.size() > 0) {
        final Region region = Region.getRegionByName(countryCode, stringValue);
        if (region == null) {
          addValidationError("Invalid Value");
          valid = false;
        } else {
          setValue(region.getName());
        }
      } else {
        setValue(stringValue);
      }
    }
    return valid;
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.ui.html.form.Field#postInit()
   */
  @Override
  public void postInit(final HttpServletRequest request) {
    final CountryField countryField = (CountryField)getForm().getField(
      "country");
    countryCode = countryField.getCountryCode();
    if (countryCode != null) {
      regions = Region.getRegions(countryCode);
    }
    if (stringValue == null) {
      setValue(getInitialValue(request));
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (regions.size() > 0) {
      serializeSelectField(out);
    } else {
      serializeTextField(out);
    }
  }

  private void serializeOptions(final XmlWriter out) {
    for (final Region region : regions) {
      out.startTag(HtmlUtil.OPTION);
      if (region.getName().equals(stringValue)) {
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
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    serializeOptions(out);
    out.endTag(HtmlUtil.SELECT);
  }

  private void serializeTextField(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    out.attribute(HtmlUtil.ATTR_SIZE, "30");
    out.attribute(HtmlUtil.ATTR_MAX_LENGTH, "30");
    if (stringValue != null) {
      out.attribute(HtmlUtil.ATTR_VALUE, stringValue);
    }
    out.endTag(HtmlUtil.INPUT);
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    stringValue = null;
    if (regions.size() > 0) {
      if (value != null) {
        final Region region = Region.getRegionByName(countryCode, (String)value);
        if (region != null) {
          stringValue = region.getName();
        }
      }
    }
  }
}
