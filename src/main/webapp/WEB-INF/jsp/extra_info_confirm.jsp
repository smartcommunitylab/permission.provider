<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="resources.internal" var="res"/>

<!DOCTYPE html>
<html>
<head>
<link rel="shortcut icon" href="img/favicon.ico" />
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
#info {
    padding: 1em;
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
.info_extra_box {
	margin: 40px 10px 40px 10px;
	text-align: justify;
}


</style>
<script src="lib/jquery.js" type="text/javascript"></script>
<script src="lib/jquery-ui.min.js" type="text/javascript"></script>
<script src="lib/bootstrap.js" type="text/javascript"></script>
<script src="lib/moment-with-locales.min.js" type="text/javascript"></script>
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
       <%--  <fmt:message bundle="${res}" key="language_label" /> : 
        <a id="enlang" href="javascript:changeLang('en')">English</a>&nbsp;|&nbsp;
        <a id="itlang" href="javascript:changeLang('it')">Italiano</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('es')">Espa&ntilde;ol</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('sr')">&#1057;&#1088;&#1087;&#1089;&#1082;&#1080;</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('sh')">Spski (latinica)</a>&nbsp;|&nbsp;
        <a href="javascript:changeLang('fi')">Suomi</a> --%>
        <div class="row">
            <img class="logo-centered" src="img/welive-logo.png" alt="Welive" />
        </div>
        <div class="row">
        
        <div class="info_extra_box">
        
        <p>
        	If you decide to register in our website to use the services provided in it, please note that TECNALIA shall be responsible for treatment of the personal data it has access to through the registration process, and shall comply with that laid down in the Directive 95/46/EC of the European Parliament and of the Council of 24 October 1995 on the protection of individuals with regard to the processing of personal data and on the free movement of such data, as well as any other applicable national regulations currently in force or introduced in the future to modify and/or replace it (Act of Parliament 15/1999, of 13 December, on Personal Data Protection). The data is gathered to manage your subscription to the Website and services, send you communications about services you have subscribed to, about new ones, or about the Website. They will be also used to calculate, anonymous statistical data or scientific studies on the use of the Website and services. We will never send you advertisements nor do we'll give up your data to a third party without asking your authorization.
		</p>
		<p>
			You will be able to exercise your rights, referred to your personal data, to access, rectify, erasure, and object at any time through the e-mail address <a href="mailto:info@welive.eu">info@welive.eu</a>.
		</p>
		<p>
			All the countries in the European Union must observe the Directive 95/46/EC of the European Parliament and of the Council of 24 October 1995 on the protection of individuals with regard to the processing of personal data and on the free movement of such data. When you select the city of Novi Sad, in Serbia, you are selecting a country that is not member of the European Union and therefore is not obliged to observe the Directive 95/46/EC. DunavNET, PUC Informatika and City of Novi Sad, our partners that process data in Serbia, are committed to the project partners and you to observe the Directive 95/46/EC and follow the instructions of the project partners when working with personal data. When you provide your personal data and select Novi Sad, you permit DunavNET, PUC Informatika and City of Novi Sad, located in Serbia, process your data in the scope of this project and its services.
        </p>
        </div>
        
        </div>

        <form:form method="POST" modelAttribute="info" action="/aac/collect-info">
            <div class="row">
            	<div class="button-row">
					<input type="submit" name="accept" value="<fmt:message bundle="${res}" key="extinfo_accept" />" class="btn btn-default" />
					<input type="submit" name="reject" value="<fmt:message bundle="${res}" key="extinfo_reject" />" class="btn btn-default" />			
            	</div>
        	</div>
        </form:form>
    </div>
    
    <script type="text/javascript">
		
	 	var userLang = navigator.language || navigator.userLanguage; 	//read the browser language
 		var passedLang ="<%=request.getAttribute("language")%>";		//read the passed language
 		var langToUse = correctLang(userLang);
 		if(passedLang && passedLang != 'null'){
 			langToUse = correctLang(passedLang);
 		}
	 	
        function correctLang(lang){
        	if(lang.indexOf("en") > -1){
        		return "en";
        	} else if(lang.indexOf("it") > -1){
        		return "it";
        	} else if(lang.indexOf("es") > -1){
        		return "es";
        	} else if(lang.indexOf("sr") > -1){
        		return "bs";
        	} else if(lang.indexOf("sh") > -1){
        		return "bs";
        	} else if(lang.indexOf("fi") > -1){
        		return "fi";
        	}
        };
        
        </script>

</body>
</html>