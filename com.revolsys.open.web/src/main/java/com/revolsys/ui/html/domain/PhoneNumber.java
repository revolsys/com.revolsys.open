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
package com.revolsys.ui.html.domain;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import com.revolsys.util.JexlUtil;

public final class PhoneNumber {
  /** The default country to use if one was not specified. */
  private static Country defaultCountry = Country.getCountry("US");

  /**
   * Format the phone number using the format for the default country. If the
   * phone number does not match the specification for that country the first
   * few digits of the phone number will be used to lookup a matching country
   * and formatted using that country, if that fails the unformatted phone
   * number will be returned.
   *
   * @param phoneNumber The normalized phone number to format.
   * @return The formatted phone number.
   */
  public static String format(final String phoneNumber) {
    return format(phoneNumber, defaultCountry);
  }

  /**
   * Format the phone number using the National format specification defined for
   * the Country. If the phone number does not match the specification for that
   * country the first few digits of the phone number will be used to lookup a
   * matching country and formatted using that country, if that fails the
   * unformatted phone number will be returned.
   *
   * @param phoneNumber The normalized phone number to format.
   * @param country The Country the phone number should be formatted for.
   * @return The formatted phone number.
   */
  public static String format(final String phoneNumber, final Country country) {
    return format(phoneNumber, country, false);
  }

  /**
   * Format the phone number using the specification defined for the Country. If
   * the international parameter is set to true then use the International phone
   * number format with the country code prefix, otherwise use the National
   * phone number format. If the phone number does not match the specification
   * for that country the first few digits of the phone number will be used to
   * lookup a matching country and formatted using that country's international
   * format, if that fails the unformatted phone number will be returned.
   *
   * @param phoneNumber The normalized phone number to format.
   * @param country The Country the phone number should be formatted for.
   * @param international True if the phone number should use the international
   *          format.
   * @return The formatted phone number.
   */
  public static String format(final String phoneNumber, final Country country,
    final boolean international) {
    String formattedNumber = null;
    if (phoneNumber == null) {
      return null;
    }
    if (country != null) {
      if (international) {
        formattedNumber = format(phoneNumber, country.getPhoneRegEx(),
          country.getPhoneInternationalFormat());
      } else {
        formattedNumber = format(phoneNumber, country.getPhoneRegEx(),
          country.getPhoneNationalFormat());
      }
    }
    if (formattedNumber == null) {
      final Country potentialCountry = Country.getCountryByPhoneNumber(phoneNumber);
      if (potentialCountry != null) {
        formattedNumber = format(phoneNumber, potentialCountry.getPhoneRegEx(),
          potentialCountry.getPhoneInternationalFormat());
      }
    }
    if (formattedNumber == null) {
      return phoneNumber;
    } else {
      return formattedNumber;
    }
  }

  /**
   * Format the phone number using the National format specification defined for
   * the Locale. If the phone number does not match the specification for that
   * locale the first few digits of the phone number will be used to lookup a
   * matching country and formatted using that locale, if that fails the
   * unformatted phone number will be returned.
   *
   * @param phoneNumber The normalized phone number to format.
   * @param locale The Locale the phone number should be formatted for.
   * @return The formatted phone number.
   */
  public static String format(final String phoneNumber, final Locale locale) {
    return format(phoneNumber, Country.getCountry(locale.getCountry()), false);
  }

  /**
   * Parse a phone number using the regular expression and if it matches the
   * phone number, format it using the specified format otherwise return null.
   *
   * @param phoneNumber The normalized phone number to format.
   * @param regex The regular expression to match phone numbers.
   * @param format The format specification.
   * @return The formatted phone number.
   */
  public static String format(final String phoneNumber, final String regex, final String format) {
    if (phoneNumber != null && regex != null && format != null) {
      final Pattern pattern = Pattern.compile(regex);
      final Matcher matcher = pattern.matcher(phoneNumber);
      if (matcher.matches()) {
        final Map values = new HashMap();
        for (int i = 1; i <= matcher.groupCount(); i++) {
          values.put("n" + i, matcher.group(i));
        }
        JexlExpression expression;
        try {
          expression = JexlUtil.newExpression(format);
        } catch (final Exception e) {
          throw new IllegalArgumentException(
            regex + " is not a valid regular expression: " + e.getMessage());
        }
        final MapContext context = new MapContext(values);
        try {
          return (String)expression.evaluate(context);
        } catch (final Exception e) {
          throw new IllegalArgumentException(format + " is not a valid format: " + e.getMessage());
        }
      }
    }
    return null;
  }

  public static boolean isValid(final String phoneNumber) {
    final String number = normalize(phoneNumber);
    return Pattern.matches("^\\d+$", number);
  }

  /**
   * Normalize the phone number removing any non-digit characters.
   *
   * @param phoneNumber The phone number.
   * @return The normalized phone number.
   */
  public static String normalize(final String phoneNumber) {
    if (phoneNumber != null) {
      return phoneNumber.replaceAll("[\\+\\(\\)\\-\\s]+", "");
    }
    return phoneNumber;
  }

  /**
   * Construct a new PhoneNumber.
   */
  private PhoneNumber() {
  }
}
