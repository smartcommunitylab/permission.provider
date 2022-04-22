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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<fmt:setBundle basename="resources.internal" var="res"/>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="-1" />        
        <title><fmt:message bundle="${res}" key="lbl_login_title" /></title>
    <!-- Design Italia -->
    <link rel="stylesheet" href="../italia/css/bootstrap-italia.min.css"/>   
    <script>window.__PUBLIC_PATH__ = '../italia/fonts'</script>    
    <link href="../italia/style.css" rel="stylesheet" />
  

    </head>
    <body>
      <div class="container">
        <img class="logo" src="../img/ls_logo.png" alt="SmartCommunity" />
        <div class="clear"></div>
        <div class="row  justify-content-center">
          <div class="col-md-offset-4 col-md-4">
            <div class="panel panel-default">
            <form:form modelAttribute="reg" action="changepwd">
                <div>&nbsp;</div>
                <c:if test="${error != null}"><div class="error"><fmt:message bundle="${res}" key="${error}" /></div></c:if>
                <div>&nbsp;</div>
                  <form:input type="hidden" path="email" value="<%=request.getSession().getAttribute(\"changePwdEmail\") %>"/>
<%--                 <div class="col-md-12 form-group">
                  <label> <fmt:message bundle="${res}" key="lbl_user" />*: </label>
                  <form:input cssClass="form-control" path="email"/>
                  <form:errors cssClass="error" path="email"/>
                </div>
 --%>                <div class="col-md-12 form-group">
                  <label> <fmt:message bundle="${res}" key="lbl_new_pwd" />*: </label>
                  <form:input class="form-control" type="password" path="password"/>
                  <form:errors cssClass="error" path="password"/>
                </div>
                <div class="col-md-12 form-group">
                  <button class="btn btn-primary"><fmt:message bundle="${res}" key="lbl_reg_btn" /></button>
                </div>
                <div>&nbsp;</div>
            </form:form>
            </div>
          </div>
        </div>
      </div>  
  <script src="../italia/js/bootstrap-italia.bundle.min.js"  type="text/javascript" charset="utf-8"></script>     
    </body>
</html>