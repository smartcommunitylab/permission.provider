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
<%@page contentType="text/html" pageEncoding="UTF8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="../css/style.css" rel="stylesheet" type="text/css">
<title>Smart Community Authentication</title>
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
}
</style>
</head>
<body>
	<img class="logo" src="../img/welive-logo.png" alt="WeLive" />
	<div class="clear"></div>
	<div class="authorities">
		<p>Please choose the provider for your login</p>
		<ul class="pprovider">
			<%
				Map<String, String> authorities = (Map<String, String>) request
						.getAttribute("authorities");
				for (String s : authorities.keySet()) {
			%>
			<li><a href="<%=request.getContextPath()%>/eauth/<%=s%>"><%=s.toUpperCase()%></a>
			</li>
			<%
				}
			%>
		</ul>
	</div>

	<footer class="footer">
		<div class="container">
			<p>
				<a class="link"
					href="https://dev.welive.eu/?p_p_id=58&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=1&saveLastPath=0&_58_struts_action=%2Flogin%2Fcreate_account"
					target="_blank">WeLive sign up</a>
			</p>
		</div>
	</footer>
</body>
</html>
