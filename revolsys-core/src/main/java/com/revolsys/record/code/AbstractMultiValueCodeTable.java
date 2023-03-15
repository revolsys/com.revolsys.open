package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.util.CaseConverter;

public abstract class AbstractMultiValueCodeTable extends AbstractCodeTable {

  public AbstractMultiValueCodeTable() {
  }

  public AbstractMultiValueCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected void addValue(final Identifier id, final List<Object> values) {
    final CodeTableData data = this.getData();
    synchronized (data) {
      final Object previousValue = data.addIdentifierAndValue(id, values);
      // if (previousValue instanceof LoadingValue) {
      // final LoadingValue loadingValue = (LoadingValue)previousValue;
      // Mono<Void> publisher = null;
      // if (!loadingValue.singleCallbacks.isEmpty()) {
      // publisher = Flux.fromIterable(loadingValue.singleCallbacks)
      // .doOnNext(callback -> callback.accept(value))
      // .then();
      // }
      // if (!loadingValue.multipleCallbacks.isEmpty()) {
      // final List<Object> listValue = Collections.singletonList(value);
      // final Mono<Void> publisher2 =
      // Flux.fromIterable(loadingValue.multipleCallbacks)
      // .doOnNext(callback -> callback.accept(listValue))
      // .then();
      // if (publisher == null) {
      // publisher = publisher2;
      // } else {
      // publisher = publisher.concatWith(publisher2).then();
      // }
      // publisher.subscribeOn(Schedulers.boundedElastic()).subscribe();
      //
      // }
      // }

      data.setValueToId(id, values);
      data.setValueToId(id, getNormalizedValues(values));
    }
  }

  protected void addValue(final Identifier id, final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    addValue(id, valueList);
  }

  protected void addValues(final Map<Identifier, List<Object>> valueMap) {
    for (final Entry<Identifier, List<Object>> entry : valueMap.entrySet()) {
      final Identifier id = entry.getKey();
      final List<Object> values = entry.getValue();
      addValue(id, values);
    }
  }

  @Override
  public AbstractMultiValueCodeTable clone() {
    return (AbstractMultiValueCodeTable)super.clone();
  }

  protected Identifier getIdByValue(final List<Object> valueList) {
    processValues(valueList);
    Identifier id = this.getData().getValueById(valueList);
    if (id == null) {
      final List<Object> normalizedValues = getNormalizedValues(valueList);
      id = this.getData().getValueById(normalizedValues);
    }
    return id;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    return getIdentifier(values, true);
  }

  protected Identifier getIdentifier(final List<Object> values, final boolean loadMissing) {
    refreshIfNeeded();
    if (values.size() == 1) {
      final Object id = values.get(0);
      final Identifier identifier = this.getData().getIdentifier(id);
      if (identifier != null) {
        return identifier;
      }
    }

    processValues(values);
    Identifier id = getIdByValue(values);
    if (id == null && loadMissing && isLoadMissingCodes() && !isLoading()) {
      final CodeTableData data = this.getData();
      synchronized (data) {
        id = loadId(values, true);
        if (id != null && !data.hasIdentifier(id)) {
          addValue(id, values);
        }
      }
    }
    return id;
  }

  @Override
  public List<Identifier> getIdentifiers() {
    refreshIfNeeded();
    return Collections.unmodifiableList(this.getData().getIdentifiers());
  }

  @Override
  public Identifier getIdExact(final List<Object> values) {
    final CodeTableData data = this.getData();
    Identifier id = data.getValueById(values);
    if (id == null) {
      synchronized (data) {
        id = loadId(values, false);
        return data.getValueById(values);
      }
    }
    return id;
  }

  private List<Object> getNormalizedValues(final List<Object> values) {
    final List<Object> normalizedValues = new ArrayList<>();
    for (final Object value : values) {
      final Object normalizedValue = getNormalizedValue(value);
      normalizedValues.add(normalizedValue);
    }
    return normalizedValues;
  }

  protected List<Object> getValueById(Object id) {
    if (this.getData().hasValue(Collections.singletonList(id))) {
      if (id instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)id;
        return Collections.singletonList(identifier.getValue(0));
      } else {
        return Collections.singletonList(id);
      }
    } else {
      List<Object> values = this.getData().getValueById(id);
      if (values == null) {
        final Identifier identifier = this.getData().getIdentifier(id);
        if (identifier != null) {
          values = this.getData().getValueById(identifier);
        }
      }
      return values;
    }
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id != null) {
      List<Object> values = getValueById(id);
      if (values == null) {
        synchronized (this.getData()) {
          values = loadValues(id);
          if (values != null && !isLoadAll()) {
            addValue(id, values);
          }
        }
      }
      if (values != null) {
        return Collections.unmodifiableList(values);
      }

    }
    return null;
  }

  protected boolean isLoadMissingCodes() {
    return false;
  }

  protected Identifier loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  protected List<Object> loadValues(final Object id) {
    return null;
  }

  private void processValues(final List<Object> valueList) {
    if (isCapitalizeWords()) {
      for (int i = 0; i < valueList.size(); i++) {
        final Object value = valueList.get(i);
        if (value != null) {
          final String newValue = CaseConverter.toCapitalizedWords(value.toString());
          valueList.set(i, newValue);
        }
      }
    }
  }

}
