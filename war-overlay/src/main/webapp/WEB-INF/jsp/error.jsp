<%@ page 
  contentType="text/html; charset=UTF-8"
  session="false"
  pageEncoding="UTF-8"
%><%@
  taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><!DOCTYPE html>
<html xml:lang="en">
  <head>
    <title><c:out value="${requestScope['javax.servlet.error.status_code']}" /> Error</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js">
    </script>
    <style>
body {
  padding-top: 50px;
}
    </style>
  </head>
  <body>
  <div class="container">
    <div class="panel panel-danger">
      <div class="panel-heading">
        <h3 class="panel-title"><c:out value="${requestScope['javax.servlet.error.status_code']}" /> Error</h3>
      </div>
      <div class="panel-body">
<c:if test="${not empty param['subTitle']}">
        <h4><c:out value="${param['subTitle']}" /></h4>
</c:if>
<c:if test="${not empty requestScope['javax.servlet.error.message']}">
        <pre><c:out value="${requestScope['javax.servlet.error.message']}" escapeXml="false" /></pre>
</c:if>
        <button type="button" onclick="history.go(-1)" class="btn btn-primary btn-sm"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span> Back</button>
        <a type="button" href="<c:url value="/" />" class="btn btn-primary btn-sm"><span class="glyphicon glyphicon-home" aria-hidden="true"></span> Home</a>
      </div>
    </div>
  </div>
  </body>
</html>
