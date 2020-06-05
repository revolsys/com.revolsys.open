package com.revolsys.core.test.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.log.LogbackUtil;
import com.revolsys.record.Record;
import com.revolsys.util.Strings;

public class TestUtil {

  public static void enableInfo(final Class<?> clazz) {
    final String name = clazz.getName();
    LogbackUtil.setLevel(name, "INFO");
    LogbackUtil.setLevel(null, "INFO");
  }

  public static void logValues(final Class<?> clazz, final Record record) {
    final Logger logger = LoggerFactory.getLogger(clazz);
    if (logger.isInfoEnabled()) {
      final List<Object> values = record.getValues();
      final String message = Strings.toString(values);
      logger.info(message);
    }
  }

  public static void logValues(final Object category, final Record record) {
    final Class<? extends Object> clazz = category.getClass();
    logValues(clazz, record);
  }
}
