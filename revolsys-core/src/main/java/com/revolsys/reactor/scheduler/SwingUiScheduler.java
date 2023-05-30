/*
 * Copyright (c) 2011-2021 VMware Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolsys.reactor.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Scheduler;
import reactor.util.context.Context;

/**
 * Scheduler that runs tasks on Swing's event dispatch thread.
 */
public final class SwingUiScheduler implements Scheduler {

  static final class SwingScheduledDirectAction extends AtomicBoolean
    implements Runnable, Disposable {
    /** */
    private static final long serialVersionUID = 2378266891882031635L;

    final Runnable action;

    public SwingScheduledDirectAction(final Runnable action) {
      this.action = action;
    }

    @Override
    public void dispose() {
      set(true);
    }

    @Override
    public void run() {
      if (!get()) {
        try {
          this.action.run();
        } catch (final Throwable ex) {
          Exceptions.throwIfFatal(ex);
          Operators.onErrorDropped(ex, Context.empty());
        }
      }
    }
  }

  static final class SwingSchedulerWorker implements Worker {

    volatile boolean unsubscribed;

    Set<Timer> tasks;

    SwingSchedulerWorker() {
      this.tasks = new HashSet<>();
    }

    @Override
    public void dispose() {
      if (this.unsubscribed) {
        return;
      }
      this.unsubscribed = true;

      Set<Timer> set;
      synchronized (this) {
        set = this.tasks;
        this.tasks = null;
      }

      if (set != null) {
        for (final Timer t : set) {
          t.stop();
        }
      }
    }

    void remove(final Timer timer) {
      if (this.unsubscribed) {
        return;
      }
      synchronized (this) {
        if (this.unsubscribed) {
          return;
        }

        this.tasks.remove(timer);
      }
    }

    @Override
    public Disposable schedule(final Runnable action) {

      if (!this.unsubscribed) {
        final SwingScheduledDirectAction a = new SwingScheduledDirectAction(action);

        SwingUtilities.invokeLater(a);

        return a;
      }
      throw Exceptions.failWithRejected();
    }

    @Override
    public Disposable schedule(final Runnable action, final long delayTime, final TimeUnit unit) {
      if (delayTime <= 0) {
        return schedule(action);
      }

      if (this.unsubscribed) {
        throw Exceptions.failWithRejected();
      }

      final Timer timer = new Timer((int)unit.toMillis(delayTime), null);
      timer.setRepeats(false);

      synchronized (this) {
        if (this.unsubscribed) {
          throw Exceptions.failWithRejected();
        }
        this.tasks.add(timer);
      }

      timer.addActionListener(e -> {
        try {
          try {
            action.run();
          } catch (final Throwable ex) {
            Exceptions.throwIfFatal(ex);
            Operators.onErrorDropped(ex, Context.empty());
          }
        } finally {
          remove(timer);
        }
      });

      timer.start();

      if (this.unsubscribed) {
        timer.stop();
        throw Exceptions.failWithRejected();
      }

      return () -> {
        timer.stop();
        remove(timer);
      };
    }

    @Override
    public Disposable schedulePeriodically(final Runnable task, final long initialDelay,
      final long period, final TimeUnit unit) {
      if (this.unsubscribed) {
        throw Exceptions.failWithRejected();
      }

      final Timer timer = new Timer((int)unit.toMillis(period), null);
      timer.setInitialDelay((int)unit.toMillis(initialDelay));

      synchronized (this) {
        if (this.unsubscribed) {
          throw Exceptions.failWithRejected();
        }
        this.tasks.add(timer);
      }

      timer.addActionListener(e -> {
        try {
          task.run();
        } catch (final Throwable ex) {
          timer.stop();
          remove(timer);
          Exceptions.throwIfFatal(ex);
          Operators.onErrorDropped(ex, Context.empty());
        }
      });

      timer.start();

      if (this.unsubscribed) {
        timer.stop();
        throw Exceptions.failWithRejected();
      }

      return () -> {
        timer.stop();
        remove(timer);
      };
    }
  }

  public static final SwingUiScheduler INSTANCE = new SwingUiScheduler();

  SwingUiScheduler() {
  }

  @Override
  public Worker createWorker() {
    return new SwingSchedulerWorker();
  }

  @Override
  public Disposable schedule(final Runnable task) {
    final SwingScheduledDirectAction a = new SwingScheduledDirectAction(task);

    SwingUtilities.invokeLater(a);

    return a;
  }

  @Override
  public Disposable schedule(final Runnable task, final long delay, final TimeUnit unit) {
    if (delay <= 0) {
      return schedule(task);
    }

    final Timer timer = new Timer((int)unit.toMillis(delay), null);
    timer.setRepeats(false);
    timer.addActionListener(e -> {
      try {
        task.run();
      } catch (final Throwable ex) {
        Exceptions.throwIfFatal(ex);
        Operators.onErrorDropped(ex, Context.empty());
      }
    });
    timer.start();
    return timer::stop;
  }

  @Override
  public Disposable schedulePeriodically(final Runnable task, final long initialDelay,
    final long period, final TimeUnit unit) {
    final Timer timer = new Timer((int)unit.toMillis(period), null);
    timer.setInitialDelay((int)unit.toMillis(initialDelay));

    timer.addActionListener(e -> {
      try {
        task.run();
      } catch (final Throwable ex) {
        timer.stop();
        Exceptions.throwIfFatal(ex);
        Operators.onErrorDropped(ex, Context.empty());
      }
    });
    timer.start();
    return timer::stop;
  }

}
