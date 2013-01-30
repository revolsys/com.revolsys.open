package com.revolsys.swing.map.style;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CartoCssDocument {

  private final List<RuleSet> ruleSets = new ArrayList<RuleSet>();

  private final Map<String, RuleSet> ruleSetsBySelector = new LinkedHashMap<String, RuleSet>();

  public CartoCssDocument(final List<RuleSet> ruleSets) {
    this.ruleSets.addAll(ruleSets);
    for (final RuleSet ruleSet : ruleSets) {
      final Selector selector = ruleSet.getSelector();
      final String selectorString = selector.toString();
      ruleSetsBySelector.put(selectorString, ruleSet);
    }
  }

  public RuleSet getRuleSet(final String selector) {
    return ruleSetsBySelector.get(selector);
  }

  public List<RuleSet> getRuleSets() {
    return ruleSets;
  }
}
