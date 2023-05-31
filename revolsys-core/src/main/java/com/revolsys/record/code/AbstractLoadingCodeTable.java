package com.revolsys.record.code;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.revolsys.io.BaseCloseable;
import com.revolsys.util.Debug;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractLoadingCodeTable extends AbstractCodeTable
  implements BaseCloseable, Cloneable {

  private final Map<Object, CodeTableLoadingEntry> loadingByValue = new HashMap<>();

  private final Sinks.Many<CodeTableData> dataSubject = Sinks.many().replay().latest();

  private final AtomicReference<Disposable> refreshDisposable = new AtomicReference<>();

  private boolean loadMissingCodes = true;

  private boolean loadAll = true;

  public AbstractLoadingCodeTable() {
  }

  protected void clearCaches() {
  }

  @Override
  public AbstractLoadingCodeTable clone() {
    return (AbstractLoadingCodeTable)super.clone();
  }

  @Override
  public CodeTableEntry getEntry(final Consumer<CodeTableEntry> callback, final Object idOrValue) {
    final CodeTableEntry entry = super.getEntry(callback, idOrValue);
    if (entry == null) {
      final Mono<CodeTableEntry> loader = loadValue(idOrValue);
      if (callback == null) {
        if (SwingUtilities.isEventDispatchThread()) {
          Debug.noOp();
        }
        return loader.block();
      } else {
        loader.subscribeOn(Schedulers.boundedElastic()).subscribe(callback);
      }
    }
    return entry;
  }

  @Override
  public boolean isLoadAll() {
    return this.loadAll;
  }

  @Override
  public boolean isLoaded() {
    return getData().isAllLoaded();
  }

  @Override
  public boolean isLoading() {
    final Disposable disposable = this.refreshDisposable.get();
    return disposable != null && !disposable.isDisposed();
  }

  public boolean isLoadMissingCodes() {
    return this.loadMissingCodes;
  }

  protected abstract Mono<CodeTableData> loadAll();

  private Mono<CodeTableEntry> loadValue(final Object value) {
    Mono<CodeTableData> loaded;
    if (!isLoaded() && isLoadAll()) {
      loaded = refreshIfNeeded$().then(Mono.fromSupplier(this::getData));
    } else if (isLoadMissingCodes()) {
      CodeTableLoadingEntry loading;
      synchronized (this.loadingByValue) {
        loading = this.loadingByValue.get(value);
        if (loading == null || loading.isExpired()) {
          loading = new CodeTableLoadingEntry(this, value);
          this.loadingByValue.put(value, loading);
        }
      }
      loaded = loading.subject().filter(b -> b).map(b -> getData());
    } else {
      return Mono.empty();
    }
    return loaded.flatMap(data -> {
      final CodeTableEntry entry = data.getEntry(value);
      if (entry == null) {
        return Mono.empty();
      } else {
        return Mono.just(entry);
      }
    });
  }

  protected abstract Mono<Boolean> loadValueDo(Object idOrValue);

  protected CodeTableData newData() {
    return new CodeTableData(this);
  }

  @Override
  public void refresh() {
    this.refresh$().block();
  }

  public Mono<CodeTableData> refresh$() {
    final Disposable disposable = loadAll().doOnSuccess(data -> {
      clearCaches();
      data.setAllLoaded(true);
    }).subscribe(data -> {
      final CodeTableData savedData = updateData(oldData -> {
        if (data.isAfter(oldData)) {
          return data;
        } else {
          return oldData;
        }
      });
      this.dataSubject.tryEmitNext(savedData);
    });
    final Disposable oldValue = this.refreshDisposable.getAndSet(disposable);
    if (oldValue != null) {
      oldValue.dispose();
    }
    return this.dataSubject.asFlux().next();
  }

  @Override
  public void refreshIfNeeded() {
    refreshIfNeeded$().block();
  }

  @Override
  public Mono<Boolean> refreshIfNeeded$() {
    if (isLoadAll()) {
      if (isLoaded() || isLoading()) {
        return this.dataSubject.asFlux().next().thenReturn(true);
      } else {
        return refresh$().thenReturn(true);
      }
    } else {
      return Mono.just(false);
    }
  }

  public AbstractLoadingCodeTable setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
    return this;
  }

  @Override
  public AbstractLoadingCodeTable setLoadMissingCodes(final boolean loadMissingCodes) {
    this.loadMissingCodes = loadMissingCodes;
    return this;
  }
}
