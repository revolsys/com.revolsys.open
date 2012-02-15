package com.revolsys.ui.web.rest.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HttpMessageConverterViewController {

  @RequestMapping("/view/httpMessageConverter")
  public void render(
    final Map<String, ?> model,
    final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    final HttpMessageConverterView view = HttpMessageConverterView.getMessageConverterView();
    if (view != null) {
      view.render(response);
    }
  }
}
