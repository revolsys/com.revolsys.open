<%@ page 
  contentType="text/html; charset=UTF-8"
  session="false"
  pageEncoding="UTF-8"
%><%@
  taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <title><c:out value="${requestScope['javax.servlet.error.status_code']}" /> Error</title>
  </head>

  <body>
    <h1><c:out value="${requestScope['javax.servlet.error.status_code']}" /> Error</h1>
<c:if test="${param['subTitle'] != null}">
    <h2><c:out value="${param['subTitle']}" /></h2>
</c:if>
<c:if test="${requestScope['javax.servlet.error.message'] != null}">
    <pre></pre><c:out value="${requestScope['javax.servlet.error.message']}" escapeXml="false" /></pre>
</c:if>
    <ul>
      <li>To return to the previous page <a href="javascript:history.go(-1)">click here</a></li>
      <li>To return to the home page <a href="<c:url value="/" />">click here</a></li>
    </ul>
  </body>
</html>
