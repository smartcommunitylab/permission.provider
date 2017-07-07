<%@page import="org.springframework.security.web.WebAttributes"%>
<%@ page import="org.springframework.security.core.AuthenticationException"%>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter"%>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<link rel="stylesheet" 	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet" />
<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">

<fmt:setBundle basename="resources.internal" var="res" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="shortcut icon" href="../img/favicon.ico" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<title>AAC</title>
<link type="text/css" rel="stylesheet" href="<c:url value="../css/style.css"/>" />

<style type="text/css">
.button-row {
	margin-top: 10px;
}

/** input within button-row class**/
.button-row input:hover {
	color: white;
	text-transform: uppercase;
	font-family: "Roboto", sans-serif;
}

.button-row input {
	color: white;
	text-transform: uppercase;
	font-family: "Roboto", sans-serif;
	border-radius: 2px
}

.logo-container {
	font-size: smaller;
	height: 100%;
	line-height: 64px;
	margin: 0 0 0 10px;
	white-space: nowrap;
	font-weight: 400;
	position: absolute;
	left: 0px;
	padding: 7px 10px;
}

.dock-logo {
	max-height: 80%;
}

.dock-menu {
	float: right !important;
	font-family: "Roboto", ​sans-serif;
	font-size: 14px;
}

.dock-menu li {
	float: left;
	list-style-type: none;
	transition: background-color .3s;
}

.dock-menu li:hover {
	background-color: rgba(0, 0, 0, 0.1);
}

.dock-menu li a {
	line-height: 64px;
	font-weight: 400;
	transition: background-color .3s;
	display: inline-block;
	padding: 0 15px;
	cursor: pointer;
	color: #040404;
	text-decoration: none;
}

.dropdown-content li>a {
	font-size: 16px;
	color: #6FAFDA;
	display: block;
	line-height: 22px;
	padding: 14px 16px;
}

.white {
	height: 64px;
	box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.16), 0 2px 10px 0
		rgba(0, 0, 0, 0.12);
	margin-bottom: 5px;
}

.welive-font {
	font-family: "Roboto", ​sans-serif;
	font-size: 16px
}

.hover-item {
	box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.25);
	border-radius: 2px;
	text: white
}

.hover-item:hover {
	text: white;
	box-shadow: 0 10px 20px rgba(0, 0, 0, 0.19), 0 6px 6px
		rgba(0, 0, 0, 0.23);
}

.button-blue {
	padding:5px;
	background-color: #6fafda;
	text: white
}

.button-blue input {
	color: white;
	text-transform: uppercase;
	font-family: "Roboto", sans-serif;
	border-radius: 2px
}

.button-blue:hover {
	background-color: #6fafda;
	text: white
}

.button-red {
	padding:5px;
	background-color: #ef5350;
	text: white
}

.button-red:hover {
	background-color: #ef5350;
	text: white
}
</style>
<script type="text/javascript">
   function changeLang(lang) {
	var str = window.location.href;
	str = str.replace(/\&language=[^\&]{2}/g, '');
	str = str.replace(/\?language=[^\&]{2}/g, '');
	if (str.indexOf('?') > 0)
		window.location.href = str + '&language=' + lang;
	else
		window.location.href = str + '?language=' + lang;

} 
</script>
<script type="text/javascript">
	var clientName ="<%=request.getAttribute("clientName")%>";
</script>
</head>

<body>

	<%@ page language="java" import="java.util.*"%>
	<%@ page import="java.util.ResourceBundle"%>
	<%
		ResourceBundle resource = ResourceBundle.getBundle("commoncore");
		String serverRedirect = resource.getString("default.redirect.url");
	%>
	<nav class="white" role="navigation" id="welive-dockbar"> <a
		class="logo-container" href="<%=serverRedirect%>"> <img
		src="../img/wl-logo.png" class="dock-logo"></img>
	</a>
	<ul class="dock-menu">
		<li>
			<div class="dropdown">
				<a class="dropdown-toggle" data-toggle="dropdown"><i
					class="material-icons" style="line-height: 64px;">translate</i></a>
				<ul class="dropdown-menu dropdown-content" role="menu"
					style="margin-left: -105px;">
					<li><a href="javascript:changeLang('en')">English</a></li>
					<li><a href="javascript:changeLang('it')">Italiano</a></li>
					<li><a href="javascript:changeLang('es')">Espa&ntilde;ol</a></li>
					<li><a href="javascript:changeLang('sr')">&#1057;&#1088;&#1087;&#1089;&#1082;&#1080;</a></li>
					<li><a href="javascript:changeLang('sh')">Spski(latinica)</a></li>
					<li><a href="javascript:changeLang('fi')">Suomi</a></li>
				</ul>
			</div>
		</li>
	</ul>
	</nav>
	
	<%
		Map<String, String> langMap = new HashMap<String, String>();
		langMap.put("en", "en_GB");
		langMap.put("it", "it_IT");
		langMap.put("es", "es_ES");
		langMap.put("sr", "sr_RS");
		langMap.put("sh", "sr_RS_latin");
		langMap.put("fi", "fi_FI");
		String lang = request.getParameter("language");
		if (lang == null && pageContext.getResponse().getLocale() != null) lang = pageContext.getResponse().getLocale().getLanguage();
		if (langMap.containsKey(lang)) {
			lang = langMap.get(lang);
		} else {
			lang = langMap.get("en");
		}

		/*
		out.println(authorities);
		if (request.getSession().getAttribute("error") != null) {
				out.print("invalid username/password");
			} */
	%>
	
<div class="clear"></div>
	<div class="container">
		<div class="row" align="center">
			<div class="col-md-12 lateralmargin">
				<h3>
					<fmt:message bundle="${res}" key="auth_title" />
				</h3>
			</div>
		</div>
		<div class="row" align="center">
			<div class="col-md-12 lateralmargin">
				<% if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null && !(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof UnapprovedClientAuthenticationException)) { %>
				<div class="error">
					<h3>
						<fmt:message bundle="${res}" key="auth_denied" />
					</h3>

					<p>
						(<%= ((AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).getMessage() %>)
					</p>
				</div>
				<% } %>
				<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />
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
							<li><c:out value="${r.name}" /></li>
						</c:forEach>
					</ul>
				</div>

				<div id="my-small-authrequest">
					<ul class="pproviderwhitesmall">
						<c:forEach items="${resources}" var="r">
							<li><c:out value="${r.name}" /></li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</div>
		<br />

		<div class="row button-row" id="my-big-authbuttons">
			<div class="col-md-offset-4 col-md-4 posrel">
				<div class="col-md-offset-3 col-md-3">
					<!-- wrapp-confirm -->
					<form id="confirmationForm" name="confirmationForm"
						action="<%=request.getContextPath()%>/oauth/authorize"
						method="post">
						<input name="user_oauth_approval" value="true" type="hidden" /> <label><input
							name="authorize"
							value="<fmt:message bundle="${res}" key="auth_authorize_btn" />"
							type="submit" class="button-blue btn btn-default hover-item" /></label>
					</form>
				</div>
				<div class="col-md-3">
					<!-- wrapp-deny -->
					<form id="denialForm" name="denialForm"
						action="<%=request.getContextPath()%>/oauth/authorize"
						method="post">
						<input name="user_oauth_approval" value="false" type="hidden" />
						<label><input name="deny"
							value="<fmt:message bundle="${res}" key="auth_deny_btn" />"
							type="submit" class="btn btn-default button-red hover-item" /></label>
					</form>
				</div>
			</div>
		</div>

		<div class="row button-row" id="my-small-authbuttons">
			<div class="col-xs-12 posrel">
				<div class="col-xs-offset-2 col-xs-4 minor-padding" align="center">
					<!-- wrapp-confirm -->
					<form id="confirmationForm" name="confirmationForm"
						action="<%=request.getContextPath()%>/oauth/authorize"
						method="post">
						<input name="user_oauth_approval" value="true" type="hidden" /> <label><input
							name="authorize"
							value="<fmt:message bundle="${res}" key="auth_authorize_btn" />"
							type="submit" class="button-blue btn btn-default hover-item" /></label>
					</form>
				</div>
				<div class="col-xs-4 minor-padding" align="center">
					<!-- wrapp-deny -->
					<form id="denialForm" name="denialForm"
						action="<%=request.getContextPath()%>/oauth/authorize"
						method="post">
						<input name="user_oauth_approval" value="false" type="hidden" /> <label><input
							name="deny"
							value="<fmt:message bundle="${res}" key="auth_deny_btn" />"
							type="submit" class="btn btn-default button-red hover-item" /></label>
					</form>
				</div>
			</div>
		</div>

	</div>
</body>
</html>
