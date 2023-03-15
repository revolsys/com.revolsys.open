package com.revolsys.record.code;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.collection.map.MapEx;
import com.revolsys.util.CaseConverter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractSingleValueCodeTable extends AbstractCodeTable {

  public static interface IncompleteValue {
  }

  private static class LoadingValue implements IncompleteValue {

    private final Set<Consumer<Object>> singleCallbacks = new LinkedHashSet<>();

    private final Set<Consumer<List<Object>>> multipleCallbacks = new LinkedHashSet<>();
  }

  private static class MissingValue implements IncompleteValue {
    private final long time = System.currentTimeMillis();
  }

  public AbstractSingleValueCodeTable() {
  }

  public AbstractSingleValueCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected AbstractSingleValueCodeTable addValue(final Identifier id, final Object value) {
    final CodeTableData data = this.getData();
    synchronized (data) {
      final Object previousValue = data.addIdentifierAndValue(id, value);
      addValueId(data, id, value);
      if (previousValue instanceof LoadingValue) {
        final LoadingValue loadingValue = (LoadingValue)previousValue;
        Mono<Void> publisher = null;
        if (!loadingValue.singleCallbacks.isEmpty()) {
          publisher = Flux.fromIterable(loadingValue.singleCallbacks)
            .doOnNext(callback -> callback.accept(value))
            .then();
        }
        if (!loadingValue.multipleCallbacks.isEmpty()) {
          final List<Object> listValue = Collections.singletonList(value);
          final Mono<Void> publisher2 = Flux.fromIterable(loadingValue.multipleCallbacks)
            .doOnNext(callback -> callback.accept(listValue))
            .then();
          if (publisher == null) {
            publisher = publisher2;
          } else {
            publisher = publisher.concatWith(publisher2).then();
          }
          publisher.subscribeOn(Schedulers.boundedElastic()).subscribe();

        }
      }
    }

    return this;
  }

  protected void addValueId(CodeTableData data, final Identifier id, final Object value) {
    data.setValueToId(id, value);
    data.setValueToId(id, getNormalizedValue(value));
  }

  protected void addValues(final Map<Identifier, List<Object>> valueMap) {
    final CodeTableData data = this.getData();
    synchronized (data) {
      for (final Entry<Identifier, List<Object>> entry : valueMap.entrySet()) {
        final Identifier id = entry.getKey();
        final List<Object> values = entry.getValue();
        addValue(id, values);
      }
    }
  }

  @Override
  public AbstractSingleValueCodeTable clone() {
    return (AbstractSingleValueCodeTable)super.clone();
  }

  @Override
  public void close() {
    super.close();
    clear();
  }

  protected Identifier getIdByValue(Object value) {
    value = processValue(value);
    final CodeTableData data = getData();
    Identifier identifier = data.getValueById(value);
    if (identifier != null) {
      return identifier;
    }
    identifier = data.getIdentifier(value);
    if (identifier != null) {
      return identifier;
    }
    final Object normalizedValue = getNormalizedValue(value);
    identifier = data.getValueById(normalizedValue);
    return identifier;
  }

  protected Identifier getIdentifier(CodeTableData data, Object value, final boolean loadMissing) {
    if (value == null) {
      return null;
    }
    refreshIfNeeded();
    final Identifier identifier = data.getIdentifier(value);
    if (identifier != null) {
      return identifier;
    }

    value = processValue(value);
    Identifier id = getIdByValue(value);
    if (id == null && loadMissing && isLoadMissingCodes() && !isLoading()) {
      synchronized (data) {
        id = loadId(data, value, true);
        if (id != null && !data.hasIdentifier(id)) {
          addValue(id, value);
        }
      }
    }
    return id;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getIdentifier(value);
    } else {
      return null;
    }
  }

  @Override
  public Identifier getIdentifier(final Object value) {
    final CodeTableData data = getData();
    return getIdentifier(data, value, true);
  }

  @Override
  public Identifier getIdentifier(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return getIdentifier(value);
    } else {
      return null;
    }
  }

  @Override
  public List<Identifier> getIdentifiers() {
    refreshIfNeeded();
    return Collections.unmodifiableList(this.getData().getIdentifiers());
  }

  @Override
  public Identifier getIdExact(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getIdExact(value);
    }
    return null;
  }

  @Override
  public Identifier getIdExact(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return super.getIdExact(value);
    } else {
      return null;
    }
  }

  @Override
  public Identifier getIdExact(final Object value) {
    final CodeTableData data = getData();
    Identifier id = data.getValueById(value);
    if (id == null) {
      synchronized (data) {
        id = loadId(data, value, false);
        return data.getValueById(value);
      }
    }
    return id;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Identifier id) {
    Object value = getValueById(id);
    if (value == null) {
      synchronized (this.getData()) {
        value = loadValue(id);
        if (value != null && !isLoadAll()) {
          addValue(id, value);
        }
      }
    }
    return (V)value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Identifier id, Consumer<V> action) {
    Object value = getValueById(id);
    if (value == null) {
      synchronized (this.getData()) {
        value = loadValue(id);
        if (value != null && !isLoadAll()) {
          addValue(id, value);
        }
      }
    }
    return (V)value;
  }

  protected final Object getValueById(Object id) {
    if (id == null) {
      return null;
    }
    Object value = this.getData().getValueById(id);

    if (value == null) {
      if (this.getData().hasValue(id)) {
        if (id instanceof SingleIdentifier) {
          final SingleIdentifier identifier = (SingleIdentifier)id;
          return identifier.getValue(0);
        } else {
          return id;
        }
      } else {
        final Identifier identifier = this.getData().getIdentifier(id);
        if (identifier != null) {
          value = this.getData().getValueById(id);
        }
      }
    }
    if (value instanceof IncompleteValue) {
      return null;
    } else {
      return value;
    }
  }

  @Override
  public final List<Object> getValues(final Identifier id) {
    final Object value = getValue(id);
    if (value == null) {
      return null;
    } else {
      return Collections.singletonList(value);
    }
  }

  @Override
  public final List<Object> getValues(final Identifier id, Consumer<List<Object>> action) {
    final Object value = getValue(id, v -> action.accept(Collections.singletonList(v)));
    if (value == null) {
      return null;
    } else {
      return Collections.singletonList(value);
    }
  }

  protected boolean isLoadMissingCodes() {
    return false;
  }

  protected Identifier loadId(CodeTableData data, final Object value, final boolean createId) {
    return null;
  }

  protected Object loadValue(final Object id) {
    return null;
  }

  private Object processValue(final Object value) {
    if (isCapitalizeWords()) {
      if (value != null) {
        return CaseConverter.toCapitalizedWords(value.toString());
      }
    }
    return value;
  }

  @Override
  public void refresh() {
    this.valueFieldLength = -1;
    super.refresh();
    clear();
  }

  public void setValues(final MapEx values) {
    for (final String key : values.keySet()) {
      final Object value = values.get(key);
      final Identifier id = Identifier.newIdentifier(key);
      addValue(id, value);
    }
  }
}
