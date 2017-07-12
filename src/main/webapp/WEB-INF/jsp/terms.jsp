<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet" />
<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">

<fmt:setBundle basename="resources.internal" var="res" />

<!DOCTYPE html>
<html>
<head>
<link rel="shortcut icon" href="img/favicon.ico" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="css/bootstrap.css" rel="stylesheet" type="text/css">
<link href="css/bootstrap-datetimepicker.min.css" rel="stylesheet"
	type="text/css">
<link href="css/style.css" rel="stylesheet" type="text/css">
<title><fmt:message bundle="${res}" key="extinfo_title" /></title>

<style type="text/css">
.note {
	vertical-align: super;
}

.highlight {
	border: 1px solid #FF0900;
	padding: 1em;
	border-radius: 1em;
	margin: 1em;
	text-align: left;
}

#policy_screen {
	padding: 1em;
}

.policy_text {
	text-align: justify;
}

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

.error {
	line-height: 2.2;
	margin-left: 5px;
}

input[type=checkbox] {
	margin-right: 5px !important;
}

.marginleft {
	margin-left: 2px;
	text-align: left;
}

.relativepos {
	position: relative;
}

.tooltip-img {
	position: absolute;
	right: 5px;
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
	font-size: 16px;
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
   padding: 14px 24px;
}

.icon-caret-down {
    margin-left: 15px;
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
	background-color: #6fafda;
	text: white
}

.button-blue:hover {
	background-color: #6fafda;
	text: white
}

.button-red {
	background-color: #ef5350;
	text: white
}

.button-red:hover {
	background-color: #ef5350;
	text: white
}


</style>

<script type="text/javascript">
  var showTerms = true;
  function changeLang(lang) {
      var str = window.location.href;
      if (str.indexOf('#')>0) str = str.substring(0,str.indexOf('#'));
      str = str.replace(/\&language=[^\&]{2}/g,'');
      str = str.replace(/\?language=[^\&]{2}/g,'');
      if (str.indexOf('?')>0) window.location.href = str +'&language='+lang;
      else window.location.href = str +'?language='+lang;
  }  
  var doAccept = function() {
      window.location.href = "./collect-info";
  }
  var doReject = function() {
      window.location.href = "./eauth/start";
  }
 </script>
</head>
<body>
	<%@ page language="java" import="java.util.*"%>
	<%@ page import="java.util.ResourceBundle"%>
	<%
		ResourceBundle resource = ResourceBundle.getBundle("commoncore");
		String serverRedirect = resource.getString("default.redirect.url");
	%>
	<nav class="white" role="navigation" id="welive-dockbar">
		<a class="logo-container" href="<%=serverRedirect%>"> <img
			src="img/wl-logo.png" class="dock-logo"></img>
		</a>
		<ul class="dock-menu">
			<li>
            	<div class="dropdown">
                	<a class="dropdown-toggle" data-toggle="dropdown"><fmt:message bundle="${res}" key="help_label"/></fmt><i class="caret icon-caret-down"></i></a>
                		<ul class="dropdown-menu dropdown-content dropdown-menu-right" role="menu">
                      		<li><a href="<%=serverRedirect%>/user-guide"><fmt:message bundle="${res}" key="help_menu_1"/></fmt></a></li>
                      		<li><a href="<%=serverRedirect%>/faq"><fmt:message bundle="${res}" key="help_menu_2"/></fmt></a></li>
                      		<li><a href="<%=serverRedirect%>/contactus"><fmt:message bundle="${res}" key="help_menu_3"/></fmt></a></li>
                      		<li role="separator" class="divider"></li>
                		</ul>
            	</div>
        	</li>
			<li>
				<div class="dropdown">
					<a class="dropdown-toggle" data-toggle="dropdown"><i
						class="material-icons" style="line-height: 64px;">translate</i></a>
					<ul class="dropdown-menu dropdown-content dropdown-menu-right" role="menu"
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
	<div class="container">
		<div class="row">
			<jsp:include page="terms/${language}.jsp" flush="true" />
		</div>

		<div class="row">
			<div class="button-row">
				<input type="button" name="accept"
					value="<fmt:message bundle="${res}" key="accept" />"
					class="button-blue btn btn-default hover-item" onclick="javascript:doAccept()" /> <input
					type="button" name="reject"
					value="<fmt:message bundle="${res}" key="reject" />"
					class="button-red btn btn-default hover-item" onclick="javascript:doReject()" />
			</div>
		</div>
	</div>
</body>
</html>