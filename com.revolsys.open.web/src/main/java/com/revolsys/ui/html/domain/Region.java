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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public final class Region {
  private static Map countryRegions = new HashMap();

  private static Map countryRegionCodeMap = new HashMap();

  private static Map countryRegionNameMap = new HashMap();

  public static Region getRegion(final String countryRegioncode) {
    final StringTokenizer st = new StringTokenizer(countryRegioncode, "-");
    final String countryCode = st.nextToken();
    final Country country = Country.getCountry(countryCode);
    if (country != null && st.hasMoreTokens()) {
      final String regionCode = st.nextToken();
      return getRegionByCode(country, regionCode);
    }
    return null;
  }

  public static Region getRegionByCode(final Country country, final String code) {
    return (Region)getRegionCodeMap(country).get(code.toUpperCase());
  }

  public static Region getRegionByCode(
    final String countryCode,
    final String code) {
    return getRegionByCode(Country.getCountry(countryCode), code);
  }

  public static Region getRegionByName(final Country country, final String name) {
    return (Region)getRegionNameMap(country).get(name.toUpperCase());
  }

  public static Region getRegionByName(
    final String countryCode,
    final String name) {
    return getRegionByName(Country.getCountry(countryCode), name);
  }

  public static Map getRegionCodeMap(final Country country) {
    Map regions = (Map)countryRegionCodeMap.get(country);
    if (regions == null) {
      loadRegions(country);
      regions = (Map)countryRegionCodeMap.get(country);
    }
    return regions;
  }

  public static Map getRegionNameMap(final Country country) {
    Map regions = (Map)countryRegionNameMap.get(country);
    if (regions == null) {
      loadRegions(country);
      regions = (Map)countryRegionNameMap.get(country);
    }
    return regions;
  }

  public static List getRegions(final Country country) {
    List regions = (List)countryRegions.get(country);
    if (regions == null) {
      loadRegions(country);
      regions = (List)countryRegions.get(country);
    }
    return regions;
  }

  public static List getRegions(final String countryCode) {
    final Country country = Country.getCountry(countryCode);
    return getRegions(country);
  }

  private static void loadRegions(final Country country) {
    InputStream in = null;
    if (country != null) {
      in = Region.class.getResourceAsStream("/com/revolsys/ui/html/domain/region/"
        + country.getCodeAplha2() + ".txt");
    }
    List regions;
    Map regionMap;
    Map regionNameMap;
    if (in == null) {
      regions = Collections.EMPTY_LIST;
      regionMap = Collections.EMPTY_MAP;
      regionNameMap = Collections.EMPTY_MAP;
    } else {
      regions = new ArrayList();
      regionMap = new HashMap();
      regionNameMap = new HashMap();
      final BufferedReader lineReader = new BufferedReader(
        new InputStreamReader(in));
      try {
        for (String line = lineReader.readLine(); line != null; line = lineReader.readLine()) {
          final StringTokenizer columns = new StringTokenizer(line, "\t");
          final String code = columns.nextToken();
          final String name = columns.nextToken();
          final Region region = new Region(country, code, name);
          regions.add(region);
          regionMap.put(region.getCode().toUpperCase(), region);
          regionNameMap.put(region.getName().toUpperCase(), region);
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    countryRegions.put(country, regions);
    countryRegionCodeMap.put(country, regionMap);
    countryRegionNameMap.put(country, regionNameMap);
  }

  private final Country country;

  private final String code;

  private final String name;

  private Region(final Country country, final String code, final String name) {
    this.country = country;
    this.code = code.intern();
    this.name = name.intern();
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Region) {
      final Region region = (Region)object;
      return region.toString().equals(toString());
    } else {
      return false;
    }
  }

  public String getCode() {
    return code;
  }

  public Country getCountry() {
    return country;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public String toString() {
    return country.getCodeAplha2() + "-" + getCode();
  }
}
