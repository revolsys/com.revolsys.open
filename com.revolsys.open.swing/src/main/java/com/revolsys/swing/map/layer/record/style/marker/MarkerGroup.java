package com.revolsys.swing.map.layer.record.style.marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.collection.list.Lists;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.CaseConverter;

public class MarkerGroup extends AbstractMarkerGroupElement {
  private List<MarkerGroup> groups = new ArrayList<>();

  private List<Marker> markers = new ArrayList<>();

  private Map<String, Marker> symbolByName = new TreeMap<>();

  public MarkerGroup() {
  }

  public MarkerGroup(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public MarkerGroup(final String name) {
    this(name, CaseConverter.toCapitalizedWords(name));
  }

  public MarkerGroup(final String name, final String title) {
    super(name, title);
  }

  public void addGroup(final MarkerGroup group) {
    this.groups.add(group);
    group.setParent(this);
  }

  public MarkerGroup addGroup(final String name) {
    final MarkerGroup group = new MarkerGroup(name);
    addGroup(group);
    return group;
  }

  public void addSymbol(final Marker symbol) {
    addSymbolDo(symbol);
  }

  protected void addSymbolDo(final Marker symbol) {
    this.markers.add(symbol);
    final String name = symbol.getName();
    this.symbolByName.put(name, symbol);
  }

  public void addSymbolsToList(final List<Marker> symbols) {
    for (final MarkerGroup group : this.groups) {
      group.addSymbolsToList(symbols);
    }
    final List<Marker> symbols2 = getSymbols();
    symbols2.sort((symbol1, symbol2) -> {
      return symbol1.getTitle().compareTo(symbol2.getTitle());
    });
    symbols.addAll(symbols2);
  }

  public List<MarkerGroup> getGroups() {
    return this.groups;
  }

  public Marker getSymbol(final String name) {
    Marker symbol = this.symbolByName.get(name);
    if (symbol == null) {
      for (final MarkerGroup group : this.groups) {
        symbol = group.getSymbol(name);
        if (symbol != null) {
          return symbol;
        }
      }
    }
    return symbol;
  }

  public List<Marker> getSymbols() {
    return Lists.toArray(this.markers);
  }

  @Override
  public String getTypeName() {
    return "markerGroup";
  }

  public void setGroups(final List<MarkerGroup> groups) {
    this.groups = new ArrayList<>();
    for (final MarkerGroup group : groups) {
      groups.add(group);
    }
  }

  public void setSymbols(final List<Marker> symbols) {
    this.symbolByName = new HashMap<>();
    this.markers = new ArrayList<>();
    for (final Marker symbol : symbols) {
      addSymbolDo(symbol);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "groups", this.groups);
    addToMap(map, "markers", this.markers);
    return map;
  }
}
