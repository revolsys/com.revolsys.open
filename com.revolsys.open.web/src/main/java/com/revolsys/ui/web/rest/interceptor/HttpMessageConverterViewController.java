package com.revolsys.ui.web.rest.interceptor;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HttpMessageConverterViewController {

  @RequestMapping("/view/httpMessageConverter")
  public void render(final HttpServletResponse response) throws Exception {
    final HttpMessageConverterView view = HttpMessageConverterView.getMessageConverterView();
    if (view != null) {
      view.render(response);
    }
  }
}
