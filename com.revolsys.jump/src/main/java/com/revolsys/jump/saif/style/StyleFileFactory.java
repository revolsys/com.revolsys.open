package com.revolsys.jump.saif.style;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.revolsys.jump.csv.CSVReader;
import com.revolsys.jump.feature.filter.NameValueFeatureFilter;
import com.revolsys.jump.feature.filter.operator.EqualsOperator;
import com.revolsys.jump.feature.filter.operator.NotEqualsOperator;
import com.revolsys.jump.feature.filter.operator.Operator;
import com.revolsys.jump.ui.style.FilterTheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public class StyleFileFactory {
  private static final Operator EQUALS_OPERATOR = new EqualsOperator();

  private static final Operator NOT_EQUALS_OPERATOR = new NotEqualsOperator();

  private Map<String, BasicStyle> defaultStyles = new HashMap<String, BasicStyle>();

  private Map<String, Boolean> layerVisible = new HashMap<String, Boolean>();

  private Map<String, List<FilterTheme>> filterThemes = new HashMap<String, List<FilterTheme>>();

  private Map<String, Integer> headerIndexes = new HashMap<String, Integer>();

  public StyleFileFactory(final File file) {
    if (file.exists()) {
      try {
        open(new FileInputStream(file));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public StyleFileFactory(final InputStream in) {
    open(in);
  }

  private void open(final InputStream in) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      CSVReader csvReader = new CSVReader(reader);
      try {
        String[] line = csvReader.readNext();
        if (line != null) {
          setHeaders(line);
          for (line = csvReader.readNext(); line != null; line = csvReader.readNext()) {
            String typeName = getValue(line, "Class");
            String label = getValue(line, "Label");
            if (typeName != null) {
              String color = getValue(line, "Color");
              String lineWidth = getValue(line, "LineWidth");
              String linePattern = getValue(line, "LinePattern");
              Boolean visible = getBooleanValue(line, "Visible");
              if (color != null && !color.equals("?")) {
                try {
                  BasicStyle style = createStyle(color, lineWidth, linePattern);
                  String attributes = getValue(line, "Attributes");
                  NameValueFeatureFilter filter = toNameValueFilter(attributes);
                  if (filter == null) {
                    defaultStyles.put(typeName, style);
                    layerVisible.put(typeName, visible);
                  } else {
                    if (filter != null) {
                      FilterTheme styleFilter = new FilterTheme(label, filter,
                        style);
                      addFilterTheme(typeName, styleFilter);
                      styleFilter.setVisible(visible.booleanValue());
                    }
                  }
                } catch (RuntimeException e) {
                  System.out.println(Arrays.asList(line));
                  throw e;
                }
              }
            }
          }
        }

      } finally {
        csvReader.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Boolean getBooleanValue(final String[] values, final String name) {
    String value = getValue(values, name);
    return Boolean.valueOf(value);
  }

  private boolean addFilterTheme(final String typeName,
    final FilterTheme filterTheme) {
    return getFilterThemes(typeName).add(filterTheme);
  }

  public static NameValueFeatureFilter toNameValueFilter(final String string) {
    if (string == null || string.trim().length() == 0) {
      return null;
    } else {
      NameValueFeatureFilter filter = new NameValueFeatureFilter();
      String[] entries = string.split(",");
      for (int i = 0; i < entries.length; i++) {
        String expressionRegEx = "(\\w+(?:\\.\\w+)*)(\\W+)(.*)";
        Pattern pattern = Pattern.compile(expressionRegEx);
        Matcher matcher = pattern.matcher(entries[i]);
        if (matcher.matches()) {
          String name = matcher.group(1);
          String operator = matcher.group(2);
          String value = matcher.group(3);
          if (operator.equals("=")) {
            filter.add(name, EQUALS_OPERATOR, value);
          } else if (operator.equals("!=")) {
            filter.add(name, NOT_EQUALS_OPERATOR, value);
          }
        }
      }
      return filter;
    }
  }

  private BasicStyle createStyle(final String colorString,
    final String lineWidth, final String linePattern) {
    Color color = getColor(colorString);
    BasicStyle style = new BasicStyle(color);
    style.setFillColor(color);
    style.setRenderingFill(false);

    style.setLineColor(color);
    if (lineWidth != null && lineWidth.length() > 0) {
      float width = Float.parseFloat(lineWidth);
      style.setLineWidth((int)(width * 4));
    }
    if (linePattern != null && linePattern.length() > 0) {
      style.setRenderingLinePattern(true);
      style.setLinePattern(linePattern);
    }
    return style;
  }

  private Color getColor(final String colorString) {
    Map<String, Color> colorMap = new HashMap<String, Color>();
    colorMap.put("red", new Color(255, 0, 0));
    colorMap.put("black", new Color(0, 0, 0));
    colorMap.put("blue", new Color(0, 0, 255));
    colorMap.put("brown", new Color(165, 42, 42));
    colorMap.put("green", new Color(0, 255, 0));
    colorMap.put("orange", new Color(255, 165, 0));
    colorMap.put("purple", new Color(128, 0, 128));
    Color color = (Color)colorMap.get(colorString);
    if (color == null) {
      return (Color)colorMap.get("black");
    } else {
      return color;
    }
  }

  private void setHeaders(final String[] headers) {
    for (int i = 0; i < headers.length; i++) {
      String name = headers[i];
      headerIndexes.put(name.replaceAll("\"", ""), i);
    }

  }

  private String getValue(final String[] values, final String name) {
    Integer headerIndex = headerIndexes.get(name);
    if (headerIndex == null) {
      return null;
    }
    if (headerIndex >= values.length) {
      return null;
    }
    String value = values[headerIndex];
    if (value != null) {
      return value.replaceAll("\"", "");
    }
    return value;
  }

  public BasicStyle getDefaultStyle(final String typeName) {
    return defaultStyles.get(typeName);
  }

  public boolean getLayerVisible(final String typeName) {
    Boolean visible = layerVisible.get(typeName);
    if (visible != null) {
      return visible;
    } else {
      return false;
    }
  }

  public List<FilterTheme> getFilterThemes(final String typeName) {
    List<FilterTheme> themes = filterThemes.get(typeName);
    if (themes == null) {
      themes = new ArrayList<FilterTheme>();
      filterThemes.put(typeName, themes);
    }
    return themes;

  }
}
