/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.web.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.ui.web.exception.ActionInitException;

public abstract class IafAction {
  /** The configuration of the action. */
  private ActionConfig config;

  /**
   * Get the configuration of the action.
   *
   * @return The configuration of the action.
   */
  public ActionConfig getConfig() {
    return this.config;
  }

  /**
   * Initialize the action.
   *
   * @param config The configuration of the action.
   * @throws ActionInitException If the action could not be initialized.
   */
  public void init(final ActionConfig config) throws ActionInitException {
    this.config = config;
  }

  /**
   * @param request The HTTP request the action is to process.
   * @param response The HTTP response the action updates.
   * @throws ServletException If there was an error running the action.
   * @throws IOException If an I/O error occurred.
   */
  public abstract void process(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException;

}
