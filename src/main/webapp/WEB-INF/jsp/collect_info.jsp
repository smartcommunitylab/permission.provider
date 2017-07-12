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
#info {
    padding: 1em;
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
	/*position: relative;*/
	text-align: left;
}
.tooltip-img {
	/*position: absolute;
	right: 5px;*/
	padding-left: 10px;
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
      str = str.replace(/\&language=[^\&]{2}/g,'');
      str = str.replace(/\?language=[^\&]{2}/g,'');
      if (str.indexOf('?')>0) window.location.href = str +'&language='+lang;
      else window.location.href = str +'?language='+lang;
  }  
</script>
</head>
<body>
	<%@ page import="java.util.ResourceBundle"%>
	<%
		ResourceBundle resource = ResourceBundle.getBundle("commoncore");
		String email = resource.getString("support.email");
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
			<h3><fmt:message bundle="${res}" key="extinfo_title" /></h3>
		</div>

		<div class="row">
			<div class="highlight">
				<p><fmt:message bundle="${res}" key="extinfo_message" /></p>
			</div>
            <c:if test="${genericError != null}">
			 <div class="error"><fmt:message bundle="${res}" key="${genericError}" />
			 <%=email%></div>
			</c:if>
			<div role="form">
				<form:form method="POST" modelAttribute="info" acceptCharset="UTF-8" action="/aac/collect-info">
					<div class="button-row">
						<input type="submit" name="save" value="<fmt:message bundle="${res}" key="extinfo_save" />" class="button-blue btn btn-default item-hover welive-font" />
					</div>
					<div class="form-group relativepos">
						<label for="pilot" class="pull-left"><fmt:message bundle="${res}" key="extinfo_pilot" />
							<span><fmt:message bundle="${res}" key="extinfo_required_field" /></span>
						</label>
						<img class="tooltip-img" src="https://dev.welive.eu/Essential-core-material-theme/images/portlet/help.png" data-toggle="tooltip" title="<fmt:message bundle="${res}" key="tooltip_city_title" />">
						<form:errors path="pilot" cssClass="error pull-left"></form:errors>
						<form:select id="pilot_city_sel" path="pilot" cssClass="form-control" onChange="updateLang(this.value);">
							<form:option value=""></form:option>
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
								name="birthdate" placeholder="dd/mm/yyyy" /> <span
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
                            <form:option value=""></form:option>
                            <form:option value="M"><fmt:message bundle="${res}" key="extinfo_gender_m" /></form:option>
                            <form:option value="F"><fmt:message bundle="${res}" key="extinfo_gender_f" /></form:option>
                        </form:select>
                    </div>
                    <div class="form-group relativepos">
                        <label for="status" class="pull-left"><fmt:message bundle="${res}" key="extinfo_work_status" />? </label>
                        <form:errors path="status" cssClass="error pull-left"></form:errors>
                        <form:select path="status" cssClass="form-control">
                            <form:option value=""></form:option>
                            <form:option value="Student"><fmt:message bundle="${res}" key="extinfo_status_student" /></form:option>
                            <form:option value="Unemployed"><fmt:message bundle="${res}" key="extinfo_status_unemployed" /></form:option>
                            <form:option value="Employedbythirdparty"><fmt:message bundle="${res}" key="extinfo_status_employed" /></form:option>
                            <form:option value="Selfemployedentrepreneur"><fmt:message bundle="${res}" key="extinfo_status_entrepreneur" /></form:option>
                            <form:option value="Retired"><fmt:message bundle="${res}" key="extinfo_status_retired" /></form:option>
                            <form:option value="Other"><fmt:message bundle="${res}" key="extinfo_status_other" /></form:option>
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
						<form:input id="preferences" path="keywords" cssClass="form-control" />
						<script>document.getElementById('preferences').placeholder = "<fmt:message bundle="${res}" key='extinfo_placeholder_tags'/>";</script>
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
                        <form:select path="developer" cssClass="form-control" disabled="false">
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
                    	<div><label>
                    	 <form:errors path="adult" cssClass="error pull-right"></form:errors>
                    	<form:checkbox path="adult" /><fmt:message bundle="${res}" key="extinfo_adult_declare" />
                    	<span><fmt:message bundle="${res}" key="extinfo_required_field" /></span>
                    	</label></div>
                    </div>
                    	    
					<div class="button-row">
						<input type="submit" name="save" value="<fmt:message bundle="${res}" key="extinfo_save" />"
							class="button-blue btn btn-default item-hover welive-font" /> 
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