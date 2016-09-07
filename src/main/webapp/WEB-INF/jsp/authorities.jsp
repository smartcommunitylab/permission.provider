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
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<fmt:setBundle basename="resources.internal" var="res"/>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="../css/style.css" rel="stylesheet" type="text/css">
<link href="../css/bootstrap.min.css" rel="stylesheet" type="text/css">
<link href="../css/font-awesome.min.css" rel="stylesheet">
<link href="../css/docs.css" rel="stylesheet" type="text/css">
<link href="../css/bootstrap-social.css" rel="stylesheet" type="text/css">
<title>WeLive AAC</title>
<style type="text/css">
a.link {
	color: #555;
	display: inline-block;
	font-family: "Roboto", sans-serif;
	font-size: 1.1em;
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
}
.langSelect {
	align: center;
	heght: 30px;
	font-size: 14px;
	margin-top: 5px;
}
</style>
<script type="text/javascript">
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
	<div class="langSelect">
		<%-- <fmt:message bundle="${res}" key="language_label" /> :  --%>
		<a id="enlang" href="javascript:changeLang('en')">English</a>&nbsp;|&nbsp;
		<a id="itlang" href="javascript:changeLang('it')">Italiano</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('es')">Espa&ntilde;ol</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('sr')">&#1057;&#1088;&#1087;&#1089;&#1082;&#1080;</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('sh')">Spski (latinica)</a>&nbsp;|&nbsp;
		<a href="javascript:changeLang('fi')">Suomi</a>
		<%-- Current Locale : ${pageContext.response.locale} --%>
	</div>
	<div class="row">
		<img class="logo-centered"  src="../img/welive-logo.png" alt="WeLive" />
		<div id="my-big-authtitle" class="col-md-offset-1 col-md-10">
			<h3><fmt:message bundle="${res}" key="authorities_ecosystem_text" /></h3>
		</div>
		<div id="my-small-authtitle" class="col-xs-offset-1 col-xs-10">
			<h3><fmt:message bundle="${res}" key="authorities_ecosystem_text" /></h3>
		</div>
	</div>
	<div class="clear"></div>
	<div class="authorities">
		<p><fmt:message bundle="${res}" key="authorities_select" /></p>
		<ul class="pprovider">
			<%
				Map<String, String> authorities = (Map<String, String>) request
						.getAttribute("authorities");
			%>
            <% if(authorities.containsKey("google")) { %>
            <li>
                <a  class="btn btn-block btn-social btn-google" href="<%=request.getContextPath()%>/eauth/google">
                    <span class="fa fa-google"></span> GOOGLE
                </a>
            </li>
            <% authorities.remove("google");} %>
            <% if(authorities.containsKey("facebook")) { %>
            <li>
                <a class="btn btn-block btn-social btn-facebook" href="<%=request.getContextPath()%>/eauth/facebook">
                    <span class="fa fa-facebook"></span> FACEBOOK
                </a>
            </li>
            <% authorities.remove("facebook");} %>
			<%
				for (String s : authorities.keySet()) {
			%>
	        <li>
         		<a href="<%=request.getContextPath()%>/eauth/<%=s%>"><%=s.toUpperCase()%></a>
			</li>
			<% } %>
		</ul>
	</div>

	<footer class="footer">
		<div class="container">
			<p>
				<a class="link"
					href="https://dev.welive.eu/?p_p_id=58&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account"
					target="_blank"><fmt:message bundle="${res}" key="authorities_signup" /></a>
			</p>
		</div>
	</footer>
</body>
</html>
