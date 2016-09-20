<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="resources.internal" var="res"/>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
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
	/*position: relative;*/
	text-align: left;
}
.tooltip-img {
	/*position: absolute;
	right: 5px;*/
	padding-left: 10px;
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
      str = str.replace(/\&language=[^\&]{2}/g,'');
      str = str.replace(/\?language=[^\&]{2}/g,'');
      if (str.indexOf('?')>0) window.location.href = str +'&language='+lang;
      else window.location.href = str +'?language='+lang;
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
			<h3><fmt:message bundle="${res}" key="extinfo_title" /></h3>
		</div>

		<div class="row">
			<div class="highlight">
				<p><fmt:message bundle="${res}" key="extinfo_message" /></p>
			</div>
            <c:if test="${genericError != null}">
			 <div class="error"><fmt:message bundle="${res}" key="${genericError}" /></div>
			</c:if>
			<div role="form">
				<form:form method="POST" modelAttribute="info" acceptCharset="UTF-8" action="/aac/collect-info">
					<div class="button-row">
						<input type="submit" name="save" value="<fmt:message bundle="${res}" key="extinfo_save" />" class="btn btn-default" />
					</div>
					<div class="form-group relativepos">
						<label for="pilot" class="pull-left"><fmt:message bundle="${res}" key="extinfo_pilot" />
							<span><fmt:message bundle="${res}" key="extinfo_required_field" /></span>
						</label>
						<img class="tooltip-img" src="https://dev.welive.eu/Essential-core-material-theme/images/portlet/help.png" data-toggle="tooltip" title="<fmt:message bundle="${res}" key="tooltip_city_title" />">
						<form:errors path="pilot" cssClass="error pull-left"></form:errors>
						<form:select id="pilot_city_sel" path="pilot" cssClass="form-control" onChange="updateLang(this.value);">
							<%-- <form:option value=""></form:option> --%>
							<form:option value="Bilbao"><fmt:message bundle="${res}" key="extinfo_pilot_bilbao" /></form:option>
							<form:option value="Novisad"><fmt:message bundle="${res}" key="extinfo_pilot_novisad" /></form:option>
							<form:option value="Uusimaa"><fmt:message bundle="${res}" key="extinfo_pilot_helsinki" /></form:option>
                            <form:option value="Trento"><fmt:message bundle="${res}" key="extinfo_pilot_trento" /></form:option>
						</form:select>
					</div>
					<div class="form-group">
						<label for="name" class="pull-left"><fmt:message bundle="${res}" key="extinfo_name" />
						<span>
							<fmt:message bundle="${res}" key="extinfo_required_field" />
						</span>
						</label>
						<form:errors path="name" cssClass="error pull-left"></form:errors>
						<form:input path="name" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="surname" class="pull-left"><fmt:message bundle="${res}" key="extinfo_surname" />
						</label>
						<form:input path="surname" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="email" class="pull-left"><fmt:message bundle="${res}" key="extinfo_email" />
						<span>
							<fmt:message bundle="${res}" key="extinfo_required_field" />
						</span>
						</label>
						<form:errors path="email" cssClass="error pull-left"></form:errors>
						<form:input path="email" cssClass="form-control" />
					</div>
					<label for="birthdate" class="pull-left"><fmt:message bundle="${res}" key="extinfo_birthday" />
					</label>
					<form:errors path="birthdate" cssClass="error pull-left"></form:errors>
					<div class="form-group" style="clear: both;">
						<div class="input-group" id="datetimepicker1">
							<input type='text' class="form-control" id="birthdate"
								name="birthdate" placeholder="30/01/1970" /> <span
								class="input-group-addon"> <span
								class="glyphicon glyphicon-calendar"></span>
							</span>
						</div>
					</div>
					<div class="form-group">
                        <label for="gender" class="pull-left"><fmt:message bundle="${res}" key="extinfo_gender" />
                        </label>
                        <form:errors path="gender" cssClass="error pull-left"></form:errors>
                        <form:select path="gender" cssClass="form-control">
                            <form:option value="M"><fmt:message bundle="${res}" key="extinfo_gender_m" /></form:option>
                            <form:option value="F"><fmt:message bundle="${res}" key="extinfo_gender_f" /></form:option>
                        </form:select>
                    </div>
					<div class="form-group">
						<label for="address" class="pull-left"><fmt:message bundle="${res}" key="extinfo_address" /> </label>
						<form:errors path="address" cssClass="error pull-left"></form:errors>
						<form:input path="address" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="city" class="pull-left"><fmt:message bundle="${res}" key="extinfo_city" /> </label>
						<form:errors path="city" cssClass="error pull-left"></form:errors>
						<form:input path="city" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="zip" class="pull-left"><fmt:message bundle="${res}" key="extinfo_zip" /> </label>
						<form:errors path="zip" cssClass="error pull-left"></form:errors>
						<form:input path="zip" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="country" class="pull-left"><fmt:message bundle="${res}" key="extinfo_country" /> </label>
						<form:errors path="country" cssClass="error pull-left"></form:errors>
						<form:select path="country" cssClass="form-control">
							<form:option value=""></form:option>
							<form:option value="IT"><fmt:message bundle="${res}" key="extinfo_country_italy" /></form:option>
							<form:option value="ES"><fmt:message bundle="${res}" key="extinfo_country_spain" /></form:option>
							<form:option value="RS"><fmt:message bundle="${res}" key="extinfo_country_serbia" /></form:option>
							<form:option value="FI"><fmt:message bundle="${res}" key="extinfo_country_finland" /></form:option>
						</form:select>
					</div>
					<div class="form-group relativepos">
						<label for="keywords" class="pull-left"><fmt:message bundle="${res}" key="extinfo_tags" /></label>
						<img class="tooltip-img" src="https://dev.welive.eu/Essential-core-material-theme/images/portlet/help.png" data-toggle="tooltip" title="<fmt:message bundle="${res}" key="tooltip_keys_title" />">
						<form:errors path="keywords" cssClass="error pull-left"></form:errors>
						<form:input path="keywords" cssClass="form-control" />
					</div>
                    <div class="form-group relativepos">
                        <label for="role" class="pull-left"><fmt:message bundle="${res}" key="extinfo_role" /> </label>
                        <img class="tooltip-img" src="https://dev.welive.eu/Essential-core-material-theme/images/portlet/help.png" data-toggle="tooltip" title="<fmt:message bundle="${res}" key="tooltip_role_title" />">
                        <form:errors path="role" cssClass="error pull-left"></form:errors>
                        <form:select path="role" cssClass="form-control">
                            <form:option value="Citizen"><fmt:message bundle="${res}" key="extinfo_role_citizen" /></form:option>
                            <form:option value="Academy"><fmt:message bundle="${res}" key="extinfo_role_academy" /></form:option>
                            <form:option value="Business"><fmt:message bundle="${res}" key="extinfo_role_business" /></form:option>
                            <form:option value="Entrepreneur"><fmt:message bundle="${res}" key="extinfo_role_entrepreneur" /></form:option>
                        </form:select>
                    </div>
					<%-- <div class="form-group" style="text-align:left;">
                        <div><label><form:checkbox path="developer" disabled="true"/><fmt:message bundle="${res}" key="extinfo_developer" /></label></div>  
					</div> --%>
					<div class="form-group relativepos">
                        <label for="role" class="pull-left"><fmt:message bundle="${res}" key="extinfo_developer" />? </label>
                        <form:errors path="developer" cssClass="error pull-left"></form:errors>
                        <form:select path="developer" cssClass="form-control" disabled="true">
                            <form:option value="false"><fmt:message bundle="${res}" key="extinfo_dev_no" /></form:option>
                            <form:option value="true"><fmt:message bundle="${res}" key="extinfo_dev_yes" /></form:option>
                        </form:select>
                    </div>
					<div class="form-group relativepos">
						<label for="language" class="pull-left"><fmt:message bundle="${res}" key="extinfo_languages" />
						</label>
						<img class="tooltip-img" src="https://dev.welive.eu/Essential-core-material-theme/images/portlet/help.png" data-toggle="tooltip" title="<fmt:message bundle="${res}" key="tooltip_language_title" />">
						<form:errors path="language" cssClass="error pull-left"></form:errors>
						<br/>
					</div>
                    <div class="form-group marginleft">
						<div><label><form:checkbox id="ck_ita_lang" path="language" value="Italian" /><fmt:message bundle="${res}" key="extinfo_lang_it" /></label></div>  
                       	<div><label><form:checkbox id="ck_spa_lang" path="language" value="Spanish" /><fmt:message bundle="${res}" key="extinfo_lang_es" /></label></div>  
                      	<div><label><form:checkbox id="ck_fin_lang" path="language" value="Finnish" /><fmt:message bundle="${res}" key="extinfo_lang_fi" /></label></div>  
                        <div><label><form:checkbox id="ck_ser_lang" path="language" value="Serbian" /><fmt:message bundle="${res}" key="extinfo_lang_rs" /></label></div>  
                        <div><label><form:checkbox id="ck_sel_lang" path="language" value="SerbianLatin" /><fmt:message bundle="${res}" key="extinfo_lang_rsl" /></label></div>  
                        <div><label><form:checkbox id="ck_eng_lang" path="language" value="English" /><fmt:message bundle="${res}" key="extinfo_lang_en" /></label></div>  
                    </div>
                    
                    <div class="form-group relativepos">
                        <label for="status" class="pull-left"><fmt:message bundle="${res}" key="extinfo_work_status" /> </label>
                        <img class="tooltip-img" src="https://dev.welive.eu/Essential-core-material-theme/images/portlet/help.png" data-toggle="tooltip" title="<fmt:message bundle="${res}" key="tooltip_role_title" />">
                        <form:errors path="status" cssClass="error pull-left"></form:errors>
                        <form:select path="status" cssClass="form-control">
                            <form:option value=""></form:option>
                            <form:option value="Student"><fmt:message bundle="${res}" key="extinfo_status_student" /></form:option>
                            <form:option value="Unemployed"><fmt:message bundle="${res}" key="extinfo_status_unemployed" /></form:option>
                            <form:option value="Employed by third party"><fmt:message bundle="${res}" key="extinfo_status_employed" /></form:option>
                            <form:option value="Self-employed / Entrepreneur"><fmt:message bundle="${res}" key="extinfo_status_entrepreneur" /></form:option>
                            <form:option value="Retired"><fmt:message bundle="${res}" key="extinfo_status_retired" /></form:option>
                            <form:option value="Other"><fmt:message bundle="${res}" key="extinfo_status_other" /></form:option>
                        </form:select>
                    </div>
                    
                    <div class="form-group relativepos">
                    	<div><label>
                    	 <form:errors path="adult" cssClass="error pull-right"></form:errors>
                    	<form:checkbox path="adult" /><fmt:message bundle="${res}" key="extinfo_adult_declare" />
                    	</label></div>
                    </div>
                    	    
					<div class="button-row">
						<input type="submit" name="save" value="<fmt:message bundle="${res}" key="extinfo_save" />"
							class="btn btn-default" /> 
					</div>
					
				</form:form>
			</div>
		</div>
	</div>

	<script type="text/javascript">
		// function to auto set the pilot city language
		function updateLang(city){
			document.getElementById("ck_ita_lang").checked = false;
			document.getElementById("ck_spa_lang").checked = false;
			document.getElementById("ck_fin_lang").checked = false;
			document.getElementById("ck_ser_lang").checked = false;
			document.getElementById("ck_sel_lang").checked = false;
			document.getElementById("ck_eng_lang").checked = false;
			if(city == "Trento"){
				document.getElementById("ck_ita_lang").checked = true;
			} else if(city == "Bilbao"){
				document.getElementById("ck_spa_lang").checked = true;
			} else if(city == "Novisad"){
				document.getElementById("ck_ser_lang").checked = true;
				document.getElementById("ck_sel_lang").checked = true;
			} else if(city == "Uusimaa"){
				document.getElementById("ck_fin_lang").checked = true;
			}
		};
		var actualCity = document.getElementById("pilot_city_sel").value;
		updateLang(actualCity);
		
		
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
        
        $(function () {
            $('#datetimepicker1').datetimepicker({
            	locale: langToUse,
            	format: 'DD/MM/YYYY', 
            	viewMode: 'years'
            });
        });
        
        $(function () {
        	var option = {
        		animation: true,
           		delay: { "show": 300, "hide": 100 },
            	placement: 'right',
            	trigger: 'hover focus click'
            }
            $('[data-toggle="tooltip"]').tooltip(option)
        });
        
        </script>
</body>
</html>