package com.revolsys.ui.web.config;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.web.exception.ActionException;

public interface AttributeLoader {

  Object getValue(HttpServletRequest request) throws ActionException;

  void init(Attribute attribute);
}
