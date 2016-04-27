<%@page import="org.springframework.security.web.WebAttributes"%>
<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="resources.internal" var="res"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
  <title>AAC</title>
  <link type="text/css" rel="stylesheet" href="<c:url value="../css/style.css"/>"/>
  <link href="../css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
  <link href="../css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css"/>
</head>

<body>
    <img class="logo" src="../img/welive-logo.png" alt="WeLive" />

  <h1><fmt:message bundle="${res}" key="auth_title" /></h1>

  <div id="content">

    <% if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null && !(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof UnapprovedClientAuthenticationException)) { %>
      <div class="error">
        <h2><fmt:message bundle="${res}" key="auth_denied" /></h2>

        <p> (<%= ((AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).getMessage() %>)</p>
      </div>
    <% } %>
    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

      <h2><fmt:message bundle="${res}" key="auth_confirm" /></h2>

      <p><fmt:message bundle="${res}" key="auth_confirm_text" var="clientName"/></p>
      <ul class="pprovider">
      <c:forEach items="${resources}" var="r">
        <li><c:out value="${r.name}"/></li>
      </c:forEach>        
      </ul>

      <form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
        <input name="user_oauth_approval" value="true" type="hidden"/>
        <label><input name="authorize" value="<fmt:message bundle="${res}" key="auth_authorize_btn" />" type="submit"/></label>
      </form>
      <form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
        <input name="user_oauth_approval" value="false" type="hidden"/>
        <label><input name="deny" value="<fmt:message bundle="${res}" key="auth_deny_btn" />" type="submit"/></label>
      </form>
  </div>

    <script src="../lib/jquery.js"></script>
    <script src="../lib/bootstrap.min.js"></script>
</body>
</html>
