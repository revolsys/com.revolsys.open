package com.revolsys.websocket.json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.util.Property;
import com.revolsys.websocket.AsyncResult;

public class JsonAsyncSender implements SendHandler {

  private final Async async;

  private final LinkedList<Map<String, Object>> messages = new LinkedList<>();

  private final Session session;

  private final Map<String, AsyncResult<Map<String, Object>>> messageCallbackById = new HashMap<>();

  private final Map<String, Map<String, Object>> messageResultById = new HashMap<>();

  public JsonAsyncSender(final Session session) {
    this.session = session;
    this.async = session.getAsyncRemote();
  }

  public void close() {
    synchronized (this.messageCallbackById) {
      for (final AsyncResult<Map<String, Object>> messageProcessor : this.messageCallbackById.values()) {
        synchronized (messageProcessor) {
          messageProcessor.notify();
        }
      }
      this.messageCallbackById.clear();
    }
  }

  private void doSendMessage(final Map<String, Object> message) {
    this.async.sendObject(message, this);
  }

  @Override
  public void onResult(final SendResult result) {
    synchronized (this.messages) {
      if (this.session.isOpen()) {
        if (!result.isOK()) {
          LoggerFactory.getLogger(getClass()).error("Error sending message", result.getException());
        }
        this.messages.removeFirst();
        if (!this.messages.isEmpty()) {
          final Map<String, Object> message = this.messages.getFirst();
          doSendMessage(message);
        } else {
          this.messages.clear();
        }
      }
    }
  }

  public <V> V sendAndWait(final Map<String, Object> message,
    final AsyncResult<Map<String, Object>> messageProcessor) {
    final String messageId = UUID.randomUUID().toString();
    message.put("messageId", messageId);
    synchronized (this.messageCallbackById) {
      this.messageCallbackById.put(messageId, messageProcessor);
    }
    synchronized (messageProcessor) {
      try {
        sendMessage(message);
        messageProcessor.wait();
        final Map<String, Object> result = this.messageResultById.remove(messageId);
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

  public synchronized void sendMessage(final Map<String, Object> message) {
    final boolean hasMessage = !this.messages.isEmpty();
    if (this.session.isOpen()) {
      this.messages.addLast(message);
      if (!hasMessage) {
        doSendMessage(message);
      }
    }
  }

  public boolean setResult(final Map<String, Object> message) {
    if (this.session.isOpen()) {
      final String messageId = Maps.getString(message, "messageId");
      if (Property.hasValue(messageId)) {
        synchronized (this.messageCallbackById) {
          final AsyncResult<Map<String, Object>> resultCallback = this.messageCallbackById.get(messageId);
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
}
