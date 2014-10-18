package com.revolsys.beans;

public class EventsEnabledState implements AutoCloseable {

  public static EventsEnabledState disabled(final EventsEnabler eventsEnabler) {
    return new EventsEnabledState(eventsEnabler, false);
  }

  public static EventsEnabledState enabled(final EventsEnabler eventsEnabler) {
    return new EventsEnabledState(eventsEnabler, true);
  }

  private final boolean originalEventsEnabled;

  private final EventsEnabler eventsEnabler;

  private final boolean eventsEnabled;

  public EventsEnabledState(final EventsEnabler eventsEnabler,
    final boolean eventsEnabled) {
    this.eventsEnabler = eventsEnabler;
    this.eventsEnabled = eventsEnabled;
    this.originalEventsEnabled = eventsEnabler.setEventsEnabled(eventsEnabled);
  }

  @Override
  public void close() {
    this.eventsEnabler.setEventsEnabled(this.originalEventsEnabled);
  }

  public EventsEnabler getEventsEnabler() {
    return this.eventsEnabler;
  }

  public boolean isEventsEnabled() {
    return this.eventsEnabled;
  }

  public boolean isOriginalEventsEnabled() {
    return this.originalEventsEnabled;
  }
}
