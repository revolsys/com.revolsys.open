package com.revolsys.websocket.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.util.Property;
import com.revolsys.websocket.AsyncResult;

public class JsonAsyncSender implements SendHandler {
  private Async async;

  private final Map<String, AsyncResult<MapEx>> messageCallbackById = new HashMap<>();

  private final Map<String, MapEx> messageResultById = new HashMap<>();

  private final LinkedList<MapEx> messages = new LinkedList<>();

  private Session session;

  private boolean open = true;

  public JsonAsyncSender() {
  }

  public void clearSession() {
    synchronized (this) {
      this.session = null;
      this.async = null;
      this.notifyAll();
    }
  }

  public void close() {
    this.open = false;
    synchronized (this) {
      notifyAll();
    }
    if (this.session != null) {
      try {
        this.session.close();
      } catch (final IOException e) {
      }
    }
    clearSession();
    synchronized (this.messageCallbackById) {
      for (final AsyncResult<MapEx> messageProcessor : this.messageCallbackById.values()) {
        synchronized (messageProcessor) {
          messageProcessor.notify();
        }
      }
      this.messageCallbackById.clear();
    }
  }

  private void doSendMessage(final MapEx message) {
    if (this.open) {
      Async async = this.async;
      while (async == null) {
        synchronized (this) {
          try {
            wait(1000);
          } catch (final InterruptedException e) {
          }
          async = this.async;
          if (!this.open) {
            return;
          }
        }
      }
      async.sendObject(message, this);
    }
  }

  public boolean isSession(final Session session) {
    return this.session == session;
  }

  @Override
  public void onResult(final SendResult result) {
    synchronized (this.messages) {
      if (this.open) {
        if (!result.isOK()) {
          Logs.error(this, "Error sending message", result.getException());
        }
        this.messages.removeFirst();
        if (!this.messages.isEmpty()) {
          final MapEx message = this.messages.getFirst();
          doSendMessage(message);
        } else {
          this.messages.clear();
        }
      }
    }
  }

  public <V> V sendAndWait(final MapEx message, final AsyncResult<MapEx> messageProcessor) {
    final String messageId = UUID.randomUUID().toString();
    message.put("messageId", messageId);
    synchronized (this.messageCallbackById) {
      this.messageCallbackById.put(messageId, messageProcessor);
    }
    synchronized (messageProcessor) {
      try {
        sendMessage(message);
        messageProcessor.wait();
        final MapEx result = this.messageResultById.remove(messageId);
        if (result == null) {
          throw new RuntimeException("No result returned: " + message);
        } else {
          return messageProcessor.getResult(result);
        }
      } catch (final Throwable e) {
        throw new RuntimeException("Error getting result: " + message, e);
      } finally {
        this.messageCallbackById.remove(messageId);
        this.messageResultById.remove(messageId);
      }
    }
  }

  public synchronized void sendMessage(final MapEx message) {
    final boolean hasMessage = !this.messages.isEmpty();
    if (this.open) {
      this.messages.addLast(message);
      if (!hasMessage) {
        doSendMessage(message);
      }
    }
  }

  public boolean setResult(final MapEx message) {
    if (this.open) {
      final String messageId = Maps.getString(message, "messageId");
      if (Property.hasValue(messageId)) {
        synchronized (this.messageCallbackById) {
          final AsyncResult<MapEx> resultCallback = this.messageCallbackById.get(messageId);
          if (resultCallback != null) {
            synchronized (resultCallback) {
              this.messageResultById.put(messageId, message);
              resultCallback.notify();
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public void setSession(final Session session) {
    synchronized (this) {
      if (this.open) {
        if (session != this.session) {
          this.session = session;
          this.async = this.session.getAsyncRemote();
        }
      }
      this.notifyAll();
    }
  }
}
