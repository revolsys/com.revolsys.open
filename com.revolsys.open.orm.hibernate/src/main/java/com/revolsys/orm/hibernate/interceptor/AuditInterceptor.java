package com.revolsys.orm.hibernate.interceptor;

import java.io.Serializable;
import java.sql.Timestamp;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditInterceptor extends EmptyInterceptor {
  /** */
  private static final long serialVersionUID = -6427254413872090516L;

  /**
   * Called before an object is updated. The modifiedBy property will be set to
   * the user name for the current thread (@link #setUserName(String)} and the
   * modificationTimestamp properties will be set to the current time.
   * 
   * @param entity The entity to delete.
   * @param id The ID of the entity.
   * @param currentState The current state of the properties.
   * @param previousState The previous state of the properties.
   * @param propertyNames The names of the properties.
   * @param types The types of the properties.
   * @return True if the object was changed.
   */
  public boolean onFlushDirty(final Object entity, final Serializable id,
    final Object[] currentState, final Object[] previousState,
    final String[] propertyNames, final Type[] types) {
    String userName = getUserName();
    boolean changed = false;
    for (int i = 0; i < propertyNames.length; i++) {
      if ("modifiedBy".equals(propertyNames[i])) {
        currentState[i] = userName;
        changed = true;
      } else if ("modificationTimestamp".equals(propertyNames[i])) {
        currentState[i] = new Timestamp(System.currentTimeMillis());
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Called before an object is saved. The createdBy, modifiedBy properties will
   * be set to the user name for the current thread (@link #setUserName(String)}
   * and the modificationTimestamp properties will be set to the current time.
   * 
   * @param entity The entity to delete.
   * @param id The ID of the entity.
   * @param state The current state of the properties.
   * @param propertyNames The names of the properties.
   * @param types The types of the properties.
   * @return True if the object was changed.
   */
  public boolean onSave(final Object entity, final Serializable id,
    final Object[] state, final String[] propertyNames, final Type[] types) {
    boolean changed = false;
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String userName = getUserName();
    for (int i = 0; i < propertyNames.length; i++) {
      if ("createdBy".equals(propertyNames[i])) {
        state[i] = userName;
        changed = true;
      } else if ("modifiedBy".equals(propertyNames[i])) {
        state[i] = userName;
        changed = true;
      } else if ("modificationTimestamp".equals(propertyNames[i])) {
        state[i] = timestamp;
        changed = true;
      } else if ("creationTimestamp".equals(propertyNames[i])) {
        state[i] = timestamp;
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Get the userName for the current thread or "unknown" if a userName was not
   * set.
   * 
   * @return The userName.
   */
  public static String getUserName() {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authetication = context.getAuthentication();
    if (authetication == null) {
      return "unknown";
    } else {
      return authetication.getName();
    }

  }
}
