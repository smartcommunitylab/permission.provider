<%--

       Copyright 2012-2013 Trento RISE

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

--%>
<%@page import="java.util.Map"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet" />
<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
<fmt:setBundle basename="resources.internal" var="res" />
<!DOCTYPE html>
<html>
<head>

<link rel="shortcut icon" href="../img/favicon.ico" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="../css/style.css" rel="stylesheet" type="text/css">
<link href="../css/bootstrap.min.css" rel="stylesheet" type="text/css">
<link href="../css/font-awesome.min.css" rel="stylesheet">
<link href="../css/docs.css" rel="stylesheet" type="text/css">
<link href="../css/bootstrap-social.css" rel="stylesheet"
	type="text/css">
<title>WeLive AAC</title>
<style type="text/css">

.button-row {
	margin-top: 10px;
}

/** input within button-row class**/
.button-row input:hover {
	background-color: #6fafda;
	color: white;
	text-transform: uppercase;
	font-family: "Roboto", sans-serif;
}

.button-row input {
	background-color: #6fafda;
	color: white;
	text-transform: uppercase;
	font-family: "Roboto", sans-serif;
	border-radius: 2px	
}

.button {
	background-color: #6fafda;
	color: white;
	text-transform: uppercase;
	border-radius: 2px
}

a.link {
	display: inline-block;
	font-family: "Roboto", sans-serif;
	/*font-size: 1.1em;*/
	padding: 0px 3px;
	text-decoration: none;
	width: auto
}

.footer {
	border-top: 1px solid #EEE;
	margin-top: 2em;
	padding: 1em;
	background-image: none;
	background-repeat: no-repeat;
	opacity: 1;
	font-family: "Roboto", sans-serif;
}

.footer a.link {
	color: #555;
}

.footer a:hover {
    color: white;
}


.langSelect {
	align: center;
	heght: 30px;
	font-size: 14px;
	margin-top: 5px;
}

.row img {
	margin-bottom: 10px
}

.form-group input {
	border-radius: 2px;
	margin-bottom: 10px;
	font-family: "Roboto", sans-serif;
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
    font-family: "Roboto",​sans-serif;
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

.dropdown-content li > a {
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
    box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.16), 0 2px 10px 0 rgba(0, 0, 0, 0.12);
    margin-bottom: 5px;
}

.welive-font {
 	font-family: "Roboto",​sans-serif;
 	font-size: 16px
}

.hover-item {
	box-shadow: 0 2px 6px 0 rgba(0,0,0,0.25);
	border-radius: 2px;
}

.hover-item:hover {
	box-shadow: 0 10px 20px rgba(0,0,0,0.19),0 6px 6px rgba(0,0,0,0.23);
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
</head>
<body>
	<%@ page language="java" import="java.util.*"%>
	<%@ page import="java.util.ResourceBundle"%>
	<%
		ResourceBundle resource = ResourceBundle.getBundle("commoncore");
		String serverRedirect = resource.getString("default.redirect.url");
	%>
	<nav class="white" role="navigation" id="welive-dockbar">
		<a class="logo-container" href="<%=serverRedirect%>"> <img src="../img/wl-logo.png" class="dock-logo"></img>
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
					<a class="dropdown-toggle" data-toggle="dropdown"><i class="material-icons" style="line-height: 64px;">translate</i></a>
					 <ul class="dropdown-menu dropdown-content dropdown-menu-right" role="menu">
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
	<%-- <div class="langSelect">
		<fmt:message bundle="${res}" key="language_label" /> : 
		<a id="enlang" href="javascript:changeLang('en')">English</a>&nbsp;|&nbsp;
		<a id="itlang" href="javascript:changeLang('it')">Italiano</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('es')">Espa&ntilde;ol</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('sr')">&#1057;&#1088;&#1087;&#1089;&#1082;&#1080;</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('sh')">Spski (latinica)</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('fi')">Suomi</a>
		Current Locale : ${pageContext.response.locale}
	</div> --%>
	<div class="clear"></div>
	<%
		Map<String, String> authorities = (Map<String, String>) request.getAttribute("authorities");
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

	<div class="row">
		<img class="logo-centered" src="../img/welive-logo.png" alt="WeLive" />
		<%
			if (authorities.containsKey("welive")) {
		%>
		<div id="my-big-authtitle" class="col-md-offset-1 col-md-10 welive-font">
			<label> <fmt:message bundle="${res}"
					key="authorities_access_text" />
			</label>
		</div>
		<div id="my-small-authtitle" class="col-xs-offset-1 col-xs-10 welive-font">
			<label> <fmt:message bundle="${res}"
					key="authorities_access_text" />
			</label>
		</div>
		<%
			}
		%>
	</div>
	<div class="clear"></div>

	<%
		if (!authorities.isEmpty()) {

			if (authorities.containsKey("welive")) {
	%>

	<div role="form">
		<form:form method="POST" acceptCharset="UTF-8"
			action="/aac/login/welive-login">
			<div class="form-group welive-font">
				<input id="username" type="text" name="username"
					placeholder="insert your email" />
				<script>
					document.getElementById('username').placeholder = "<fmt:message bundle="${res}" key='authorities_username_ptext'/>";
				</script>
				<div>
					<input id="password" type="password" name="password"
						placeholder="password" />
					<script>
						document.getElementById('password').placeholder = "<fmt:message bundle="${res}" key='authorities_password_ptext'/>";
					</script>
				</div>
				<a class="link welive-font"
					href=<%=serverRedirect
								+ "/web/guest/overlay?p_p_id=58&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_58_struts_action=%2Flogin%2Fforgot_password"%>
					"
					target="_blank"><fmt:message bundle="${res}"
						key="authorities_forgot_pass_text" /></a>
				<div class="button-row welive-font">
					<input type="submit" name="login"
						value="<fmt:message bundle="${res}" key="authorities_welive_login_button_text" />"
						class="btn btn-default welive-font hover-item" />
				</div>
				<%
					if (request.getSession().getAttribute("error") != null) {
				%>
				<div>
					<label style="color:red;"> <fmt:message bundle="${res}"
							key="authorities_access_denied_text" />
					</label>
				</div>
				<%
					}
				%>
			</div>
		</form:form>
	</div>
	<%
		}
	%>
	<%
		if (authorities.containsKey("google") || authorities.containsKey("facebook")) {
	%>
	<div class="welive-font">

		<%
			if (authorities.containsKey("welive")) {
		%>
		<p>
			<fmt:message bundle="${res}" key="authorities_select" />
		</p>
		<%
			} else {
		%>
		<p>
			<fmt:message bundle="${res}" key="authorities_select_without_welive" />
		</p>
		<%
			}
		%>

		<div class="row">
			<div class="col">
				<%
					if (authorities.containsKey("google")) {
				%>
				<a class="btn btn-google welive-font hover-item"
					href="<%=request.getContextPath()%>/eauth/google">GOOGLE</a>
				<%
					}
				%>
				<%
					if (authorities.containsKey("facebook")) {
				%>
				<a class="btn btn-facebook welive-font hover-item"
					href="<%=request.getContextPath()%>/eauth/facebook">FACEBOOK </a>
				<%
					}
				%>
			</div>
		</div>

	</div>
	<%
		}
	%>
	<%
		}
	%>

	<footer class="footer">
		<div class="row">
			<div class="col">
				<a class="link welive-font" target="_blank"><fmt:message bundle="${res}"
						key="authorities_register_text" /></a> <a
					class="btn button welive-font hover-item"
					href=<%=serverRedirect + "/" + lang
					+ "/?p_p_id=58&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account"%>><fmt:message
						bundle="${res}" key="authorities_register_button_text" /></a>

			</div>
		</div>
	</footer>
</body>
</html>
