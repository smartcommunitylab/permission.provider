<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="resources.internal" var="res"/>

<!DOCTYPE html>
<html>
<head>
<link rel="shortcut icon" href="img/favicon.ico"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="css/bootstrap.css" rel="stylesheet" type="text/css">
<link href="css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
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
    margin-bottom: 1em;
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


</style>
<script src="lib/jquery.js" type="text/javascript"></script>
<script src="lib/jquery-ui.min.js" type="text/javascript"></script>
<script src="lib/bootstrap.js" type="text/javascript"></script>
<script src="lib/moment-with-locales.min.js" type="text/javascript"></script>
<script src="lib/bootstrap-datetimepicker.min.js" type="text/javascript"></script>
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
    <div class="container">
        <%-- <fmt:message bundle="${res}" key="language_label" /> :  --%>
        <a id="enlang" href="javascript:changeLang('en')">English</a>&nbsp;|&nbsp;
        <a id="itlang" href="javascript:changeLang('it')">Italiano</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('es')">Espa&ntilde;ol</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('sr')">&#1057;&#1088;&#1087;&#1089;&#1082;&#1080;</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('sh')">Spski (latinica)</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('fi')">Suomi</a>
        <%-- Current Locale : ${pageContext.response.locale} --%>
        <div class="row">
            <img class="logo-centered" src="img/welive-logo.png" alt="Welive" />
        </div>
        <div class="row">
        <jsp:include page="terms/${language}.jsp" flush="true" />
        </div>

        <div class="row">
            <div class="button-row">
                <input type="button" name="accept" value="<fmt:message bundle="${res}" key="accept" />" class="btn btn-default" onclick="javascript:doAccept()"/> 
                <input type="button" name="reject" value="<fmt:message bundle="${res}" key="reject" />" class="btn btn-default" onclick="javascript:doReject()"/> 
            </div>
        </div>
    </div>
</body>
</html>