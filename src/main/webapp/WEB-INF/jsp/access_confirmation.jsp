<%@ page contentType="text/html; charset=UTF-8" %>
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
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>AAC</title>
  <link type="text/css" rel="stylesheet" href="<c:url value="../css/style.css"/>"/>
  <link href="../css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
  <link href="../css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css"/>
	<script type="text/javascript">
	  var showTerms = true;
	  function changeLang(lang) {
	      var str = window.location.href;
	      str = str.replace(/\&language=[^\&]{2}/g,'');
	      str = str.replace(/\?language=[^\&]{2}/g,'');
	      if (str.indexOf('?')>0) window.location.href = str +'&language='+lang;
	      else window.location.href = str +'?language='+lang;
	  }  
	</script>
</head>

<body>
    <div class="container">
<%--         <fmt:message bundle="${res}" key="language_label" /> :  --%>
        <a id="enlang" href="javascript:changeLang('en')">English</a>&nbsp;|&nbsp;
        <a id="itlang" href="javascript:changeLang('it')">Italiano</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('es')">Espa&ntilde;ol</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('sr')">&#1057;&#1088;&#1087;&#1089;&#1082;&#1080;</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('sh')">Spski (latinica)</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('fi')">Suomi</a>

	    <div class="row">
	    	<img class="logo-centered" src="../img/welive-logo.png" alt="WeLive" />
	    </div>
	    <div class="row" align="center">
	    	<div class="col-md-12 lateralmargin">
	  			<h3><fmt:message bundle="${res}" key="auth_title" /></h3>
			</div>
		</div>
			<div class="row" align="center">
				<div class="col-md-12 lateralmargin">
				    <% if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null && !(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof UnapprovedClientAuthenticationException)) { %>
				    <div class="error">
				    	<h3><fmt:message bundle="${res}" key="auth_denied" /></h3>
				
				    	<p> (<%= ((AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).getMessage() %>)</p>
				    </div>
				    <% } %>
			    	<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>
			    	<h4>
			    		<fmt:message bundle="${res}" key="auth_confirm_text1" />
			    		<strong><c:out value="${clientName}"></c:out></strong>
			    		<fmt:message bundle="${res}" key="auth_confirm_text2" />
			    	</h4>
		    	</div>
		    </div>
		    
		    <div class="row">
		      <div class="col-md-12">
	            <div id="my-big-authrequest">
	                <ul class="pproviderwhite">
	                    <c:forEach items="${resources}" var="r">
	                        <li><c:out value="${r.name}"/></li>
	                    </c:forEach>        
	                </ul>
	            </div>
	            
	            <div id="my-small-authrequest">
	                <ul class="pproviderwhitesmall">
	                    <c:forEach items="${resources}" var="r">
	                        <li><c:out value="${r.name}"/></li>
	                    </c:forEach>        
	                </ul>
	            </div>
		      </div>
		    </div>
		    <br/>
		
			<div class="row" id="my-big-authbuttons">
		    	<div class="col-md-offset-4 col-md-4 posrel">
			    	<div class="col-md-offset-3 col-md-3"><!-- wrapp-confirm -->
				    	<form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
				    		<input name="user_oauth_approval" value="true" type="hidden"/>
				    		<label><input name="authorize" value="<fmt:message bundle="${res}" key="auth_authorize_btn" />" type="submit" class="btn btn-default"/></label>
				    	</form>
		    		</div>
		    		<div class="col-md-3"><!-- wrapp-deny -->
					    <form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
					    	<input name="user_oauth_approval" value="false" type="hidden" />
					    	<label><input name="deny" value="<fmt:message bundle="${res}" key="auth_deny_btn" />" type="submit" class="btn btn-default"/></label>
					    </form>
				   	</div>
		    	</div>
		    </div>
		    
		    <div class="row" id="my-small-authbuttons">
		    	<div class="col-xs-12 posrel">
			    	<div class="col-xs-offset-2 col-xs-4 minor-padding" align="center"><!-- wrapp-confirm -->
				    	<form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
				    		<input name="user_oauth_approval" value="true" type="hidden"/>
				    		<label><input name="authorize" value="<fmt:message bundle="${res}" key="auth_authorize_btn" />" type="submit" class="btn btn-default"/></label>
				    	</form>
		    		</div>
		    		<div class="col-xs-4 minor-padding" align="center"><!-- wrapp-deny -->
					    <form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
					    	<input name="user_oauth_approval" value="false" type="hidden"/>
					    	<label><input name="deny" value="<fmt:message bundle="${res}" key="auth_deny_btn" />" type="submit" class="btn btn-default"/></label>
					    </form>
				   	</div>
		    	</div>
		    </div>
		    
  	</div>

    <script src="../lib/jquery.js"></script>
    <script src="../lib/bootstrap.min.js"></script>
    <script type="text/javascript">
 		var clientName ="<%=request.getAttribute("clientName")%>";
 	</script>	
</body>
</html>
