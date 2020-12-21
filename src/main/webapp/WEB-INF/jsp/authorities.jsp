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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="resources.internal" var="res"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta name="description" content=""/>
    <meta name="author" content=""/>
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="-1" />
    <!-- Design Italia -->
    <link rel="stylesheet" href="../italia/css/bootstrap-italia.min.css"/>   
    <script>window.__PUBLIC_PATH__ = '../italia/fonts'</script>    
    <link href="../italia/style.css" rel="stylesheet" />

    <title><fmt:message bundle="${res}" key="lbl_login" /></title>
</head>
<body>
  <img class="logo d-none d-md-block" src="../img/ls_logo.png" alt="SmartCommunity" />
  <div class="clear"></div>
<% 
  Map<String, String> authorities = (Map<String,String>)request.getSession().getAttribute("authorities");
  String error = (String)request.getAttribute("error");
%>
  <div class="row mt-md-0 mt-5 justify-content-center">
    <div class="col-xl-offset-4 col-xl-4 col-lg-offset-3 col-lg-6 col-md-8 col-md-offset-2 col-sm-offset-1 col-sm-10">
      <% if (authorities.get("internal") != null) { %>
      <div class="panel panel-default">
      <h4 class="text-center"><fmt:message bundle="${res}" key="lbl_login" /></h4>
      <form action="../internal/login" method="post">
          <% if (error != null) {%>
          <div class="text-center error"><fmt:message bundle="${res}" key="${error}" /></div>
          <%} %>
          <div>&nbsp;</div>
          <div class="col-md-12 form-group">
            <label  for="username"><fmt:message bundle="${res}" key="lbl_user" />: </label>
            <input class="form-control" type="text" id="username" name="username"/>
          </div>
          <div class="col-md-12 form-group">
            <label for="password"><fmt:message bundle="${res}" key="lbl_pwd" />: </label>
            <input class="form-control input-password" type="password" id="password" name="password"/>
            <span class="password-icon" aria-hidden="true">
              <svg class="password-icon-visible icon icon-sm"><use xlink:href="../italia/svg/sprite.svg#it-password-visible"></use></svg>
              <svg class="password-icon-invisible icon icon-sm d-none"><use xlink:href="../italia/svg/sprite.svg#it-password-invisible"></use></svg>
            </span>
            <div class="col-md-12 mt-2"><a class="" href="../internal/reset""><fmt:message bundle="${res}" key="lbl_pwd_reset" /></a></div>
            <div class="row px-2 mt-4 align-items-center">
              <div class="col-md-8"><a class="" href="../internal/register"><fmt:message bundle="${res}" key="lbl_register" /></a></div>
              <div class="col-md-4 text-right"><button class="btn btn-primary"><fmt:message bundle="${res}" key="lbl_login" /></button></div>
            </div>
          </div>
      </form>
      </div>
      <% } %>
      
      <% if (authorities.size() > 1) {%>
      <div class="text-center mt-n4">
        <% if (authorities.get("internal") != null) { %>
        <p ><fmt:message bundle="${res}" key="lbl_enter_with_alt" /></p>
        <% } else { %>
        <p ><fmt:message bundle="${res}" key="lbl_enter_with" /></p>
        <% } %>  
        <div class="row  d-md-none justify-content-center">
          <% if (authorities.get("adc") != null) {%>
          <div class="col col-12 mb-3" >
                <div><a class="btn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("adc") %>">CPS / SPID</a></div>      
          </div>
          <%} %>
          <% if (authorities.get("cie") != null) {%>
          <div class="col col-12 mb-3">
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("cie") %>">CIE</a></div>      
          </div>
          <%} %>
          <% if (authorities.get("google") != null) {%>
          <div class="col col-12 mb-3">
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("google") %>">GOOGLE</a></div>      
          </div>
          <%} %>
          <% if (authorities.get("facebook") != null) {%>
          <div class="col col-12 mb-3">
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("facebook") %>">FACEBOOK</a></div>      
          </div>
          <%} %>
      </div>
      <div class="row d-none d-md-flex justify-content-center">
          <% if (authorities.get("adc") != null) {%>
          <div class="col mb-3" >
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("adc") %>">CPS / SPID</a></div>      
          </div>
          <%} %>
          <% if (authorities.get("cie") != null) {%>
          <div class="col mb-3">
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("cie") %>">CIE</a></div>      
          </div>
          <%} %>
          <% if (authorities.get("google") != null) {%>
          <div class="col mb-3">
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("google") %>">GOOGLE</a></div>      
          </div>
          <%} %>
          <% if (authorities.get("facebook") != null) {%>
          <div class="col mb-3">
                <div><a class="btn loginbtn btn-block btn-outline-primary" href="<%=request.getContextPath() %>/eauth/<%=authorities.get("facebook") %>">FACEBOOK</a></div>      
          </div>
          <%} %>
      </div>
      <% } %>
      
    </div>  
            
    </div>
  </div>  

  <script src="../italia/js/bootstrap-italia.bundle.min.js"  type="text/javascript" charset="utf-8"></script>     
</body>
</html>
