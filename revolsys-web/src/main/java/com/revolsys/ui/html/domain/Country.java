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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.revolsys.io.FileUtil;

/**
 * @author Paul Austin
 * @version 1.0
 */
public final class Country implements Serializable {
  private static List<Country> countries;

  private static Map<String, Country> countryCodeAlpha2Map = new HashMap<>();

  private static Map<String, Country> countryNameMap = new HashMap<>();

  private static Map<String, Country> countryPhoneCodeMap = new HashMap<>();

  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -3530333279679048002L;

  static {
    loadCountryCodes();
  }

  /**
   * Get the list of all countries.
   *
   * @return The list of countries.
   */
  public static List<Country> getCountries() {
    return countries;
  }

  /**
   * Get the Country by the ISO 2 character code (ignoring case).
   *
   * @param codeAplha2 The ISO 2 character code.
   * @return The country or null if not found.
   */
  public static Country getCountry(final String codeAplha2) {
    if (codeAplha2 == null) {
      return null;
    }
    return countryCodeAlpha2Map.get(codeAplha2.toUpperCase());
  }

  /**
   * Get a Country by it's name (ignoring case).
   *
   * @param name The country name.
   * @return The country or null if not found.
   */
  public static Country getCountryByName(final String name) {
    if (name == null) {
      return null;
    }
    return countryNameMap.get(name.toUpperCase());
  }

  /**
   * Get a Country by it's ITU-T phone country code.
   *
   * @param code The ITU-T phone country code.
   * @return The country or null if not found.
   */
  public static Country getCountryByPhoneCode(final String code) {
    if (code == null) {
      return null;
    }
    return countryPhoneCodeMap.get(code);
  }

  /**
   * Get a Country for a phone number.
   *
   * @param phoneNumber The normalized phone number.
   * @return The country or null if not found.
   */
  public static Country getCountryByPhoneNumber(final String phoneNumber) {
    if (phoneNumber != null && phoneNumber.length() > 0) {
      for (int i = 1; i <= 3; i++) {
        final String phoneCode = phoneNumber.substring(0, i);
        final Country country = countryPhoneCodeMap.get(phoneCode);
        if (country != null) {
          return country;
        }
      }
    }
    return null;
  }

  /**
   * Load the list of countries from the
   * com.revolsys.iaf.core.domain.CountryCodes.txt resource.
   */
  private static void loadCountryCodes() {
    if (countries == null) {
      countries = new ArrayList<>();
      final InputStream in = Country.class.getResourceAsStream("CountryCodes.txt");
      if (in != null) {
        final BufferedReader lineReader = new BufferedReader(FileUtil.newUtf8Reader(in));
        try {
          String line = lineReader.readLine();
          for (line = lineReader.readLine(); line != null; line = lineReader.readLine()) {
            final StringTokenizer columns = new StringTokenizer(line, "\t");
            final String alpha2 = columns.nextToken();
            final String alpha3 = columns.nextToken();
            final short num = Short.parseShort(columns.nextToken());
            final String name = columns.nextToken();
            String phoneCode = null;
            if (columns.hasMoreTokens()) {
              phoneCode = columns.nextToken();
            }
            String phoneRegEx = null;
            if (columns.hasMoreTokens()) {
              phoneRegEx = columns.nextToken();
            }
            String phoneNationalFormat = null;
            if (columns.hasMoreTokens()) {
              phoneNationalFormat = columns.nextToken();
            }
            String phoneInternationalFormat = null;
            if (columns.hasMoreTokens()) {
              phoneInternationalFormat = columns.nextToken();
            }
            final Country country = new Country(num, alpha2, alpha3, name, phoneCode, phoneRegEx,
              phoneNationalFormat, phoneInternationalFormat);
            countries.add(country);
            countryCodeAlpha2Map.put(country.getCodeAplha2(), country);
            countryNameMap.put(name.toUpperCase(), country);
            countryPhoneCodeMap.put(phoneCode, country);
          }
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  private final String codeAlpha3;

  private final String codeAplha2;

  private final short codeNum;

  private final String name;

  private String phoneCode;

  private String phoneInternationalFormat;

  private String phoneNationalFormat;

  private String phoneRegEx;

  private Country(final short codeNum, final String codeAplha2, final String codeAlpha3,
    final String name) {
    this.codeNum = codeNum;
    this.codeAplha2 = codeAplha2.toUpperCase().intern();
    this.codeAlpha3 = codeAlpha3.toUpperCase().intern();
    this.name = name.intern();
  }

  private Country(final short codeNum, final String codeAplha2, final String codeAlpha3,
    final String name, final String phoneCode, final String phoneRegEx,
    final String phoneNationalFormat, final String phoneInternationalFormat) {
    this.codeNum = codeNum;
    this.codeAplha2 = codeAplha2.toUpperCase().intern();
    this.codeAlpha3 = codeAlpha3.toUpperCase().intern();
    this.name = name.intern();
    this.phoneCode = phoneCode;
    this.phoneRegEx = phoneRegEx;
    this.phoneNationalFormat = phoneNationalFormat;
    this.phoneInternationalFormat = phoneInternationalFormat;
  }

  /**
   * @return
   */
  public String getCodeAlpha3() {
    return this.codeAlpha3;
  }

  /**
   * @return
   */
  public String getCodeAplha2() {
    return this.codeAplha2;
  }

  /**
   * @return
   */
  public short getCodeNum() {
    return this.codeNum;
  }

  /**
   * @return
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return Returns the phoneCode.
   */
  public String getPhoneCode() {
    return this.phoneCode;
  }

  /**
   * @return Returns the phoneInternationalFormat.
   */
  public String getPhoneInternationalFormat() {
    return this.phoneInternationalFormat;
  }

  /**
   * @return Returns the phoneNationalFormat.
   */
  public String getPhoneNationalFormat() {
    return this.phoneNationalFormat;
  }

  /**
   * @return Returns the phoneRegEx.
   */
  public String getPhoneRegEx() {
    return this.phoneRegEx;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
