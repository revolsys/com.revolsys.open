package com.revolsys.swing.map.symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.util.CaseConverter;

public class SymbolGroup extends AbstractSymbolElement {
  private List<SymbolGroup> groups = new ArrayList<>();

  private List<Symbol> symbols = new ArrayList<>();

  private Map<String, Symbol> symbolByName = new TreeMap<>();

  public SymbolGroup() {
  }

  public SymbolGroup(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public SymbolGroup(final String name) {
    this(name, CaseConverter.toCapitalizedWords(name));
  }

  public SymbolGroup(final String name, final String title) {
    super(name, title);
  }

  public void addSymbol(final Symbol symbol) {
    addSymbolDo(symbol);
  }

  protected void addSymbolDo(final Symbol symbol) {
    this.symbols.add(symbol);
    final String name = symbol.getName();
    this.symbolByName.put(name, symbol);
  }

  public SymbolGroup addSymbolGroup(final String name) {
    final SymbolGroup symbolGroup = new SymbolGroup(name);
    addSymbolGroup(symbolGroup);
    return symbolGroup;
  }

  public void addSymbolGroup(final SymbolGroup symbolGroup) {
    this.groups.add(symbolGroup);
    symbolGroup.setParent(this);
  }

  public ShapeSymbol addSymbolShape(final String name) {
    final String title = CaseConverter.toCapitalizedWords(name);
    return addSymbolShape(name, title);
  }

  public ShapeSymbol addSymbolShape(final String name, final String title) {
    final ShapeSymbol symbol = new ShapeSymbol(name, title);
    addSymbol(symbol);
    return symbol;
  }

  public void addSymbolsToList(final List<Symbol> symbols) {
    for (final SymbolGroup group : this.groups) {
      group.addSymbolsToList(symbols);
    }
    final List<Symbol> symbols2 = getSymbols();
    symbols2.sort((symbol1, symbol2) -> {
      return symbol1.getTitle().compareTo(symbol2.getTitle());
    });
    symbols.addAll(symbols2);
  }

  public SvgSymbol addSymbolSvg(final String name) {
    final SvgSymbol symbol = new SvgSymbol(name);
    addSymbol(symbol);
    return symbol;
  }

  public SvgSymbol addSymbolSvg(final String name, final String title) {
    final SvgSymbol symbol = new SvgSymbol(name, title);
    addSymbol(symbol);
    return symbol;
  }

  public List<SymbolGroup> getGroups() {
    return this.groups;
  }

  public Symbol getSymbol(final String name) {
    Symbol symbol = this.symbolByName.get(name);
    if (symbol == null) {
      for (final SymbolGroup group : this.groups) {
        symbol = group.getSymbol(name);
        if (symbol != null) {
          return symbol;
        }
      }
    }
    return symbol;
  }

  public List<Symbol> getSymbols() {
    return Lists.toArray(this.symbols);
  }

  @Override
  public String getTypeName() {
    return "symbolGroup";
  }

  public void setGroups(final List<SymbolGroup> groups) {
    this.groups = new ArrayList<>();
    for (final SymbolGroup group : groups) {
      groups.add(group);
    }
  }

  public void setSymbols(final List<Symbol> symbols) {
    this.symbolByName = new HashMap<>();
    this.symbols = new ArrayList<>();
    for (final Symbol symbol : symbols) {
      addSymbolDo(symbol);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "groups", this.groups);
    addToMap(map, "symbols", this.symbols);
    return map;
  }
}
