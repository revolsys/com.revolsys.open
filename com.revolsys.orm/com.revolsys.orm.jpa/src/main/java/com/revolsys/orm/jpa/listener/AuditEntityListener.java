package com.revolsys.orm.jpa.listener;

import java.sql.Timestamp;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.beanutils.BeanUtils;

public class AuditEntityListener {
  private String userId = "test";

  @PrePersist
  public void prePersist(final Object bean) {
    try {
      BeanUtils.setProperty(bean, "creationTimestamp", new Timestamp(
        System.currentTimeMillis()));
    } catch (Exception e) {
    }
    try {
      BeanUtils.setProperty(bean, "createdBy", userId);
    } catch (Exception e) {
    }
    preUpdate(bean);
  }

  @PreUpdate
  public void preUpdate(final Object bean) {
    try {
      BeanUtils.setProperty(bean, "modificationTimestamp", new Timestamp(
        System.currentTimeMillis()));
    } catch (Exception e) {
    }
    try {
      BeanUtils.setProperty(bean, "modifiedBy", userId);
    } catch (Exception e) {
    }
  }
}
