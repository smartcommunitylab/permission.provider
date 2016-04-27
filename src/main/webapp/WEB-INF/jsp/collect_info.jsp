<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="resources.internal" var="res"/>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="css/bootstrap.min.css" rel="stylesheet" type="text/css">
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
    margin-bottom: 1em;
}
.error {
    line-height: 2.2;
    margin-left: 5px;
}
input[type=checkbox] {
    margin-right: 5px !important;
}

</style>
</head>
<body>
	<div class="container">
		<div class="row">
			<img class="logo" src="img/welive-logo.png" alt="Welive" />
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
				<form:form method="POST" modelAttribute="info"
					action="/aac/collect-info">
					<div class="button-row">
						<input type="submit" name="save" value="Save" class="btn btn-default" />
					</div>
					<div class="form-group">
						<label for="pilot" class="pull-left"><fmt:message bundle="${res}" key="extinfo_pilot" /><span
							class="note">*</span>:
						</label>
						<form:errors path="pilot" cssClass="error pull-left"></form:errors>
						<form:select path="pilot" cssClass="form-control">
							<form:option value=""></form:option>
							<form:option value="Trento"><fmt:message bundle="${res}" key="extinfo_pilot_trento" /></form:option>
							<form:option value="Bilbao"><fmt:message bundle="${res}" key="extinfo_pilot_bilbao" /></form:option>
							<form:option value="Novisad"><fmt:message bundle="${res}" key="extinfo_pilot_novisad" /></form:option>
							<form:option value="Helsinki"><fmt:message bundle="${res}" key="extinfo_pilot_helsinki" /></form:option>
						</form:select>
					</div>
					<div class="form-group">
						<label for="name" class="pull-left"><fmt:message bundle="${res}" key="extinfo_name" /><span class="note">*</span>:
						</label>
						<form:errors path="name" cssClass="error pull-left"></form:errors>
						<form:input path="name" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="surname" class="pull-left"><fmt:message bundle="${res}" key="extinfo_surname" /><span
							class="note">*</span>:
						</label>
						<form:errors path="surname" cssClass="error pull-left"></form:errors>
						<form:input path="surname" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="email" class="pull-left"><fmt:message bundle="${res}" key="extinfo_email" /><span
							class="note">*</span>:
						</label>
						<form:errors path="email" cssClass="error pull-left"></form:errors>
						<form:input path="email" cssClass="form-control" />
					</div>
                    <div class="form-group">
                        <label for="gender" class="pull-left"><fmt:message bundle="${res}" key="extinfo_gender" />:
                        </label>
                        <form:errors path="gender" cssClass="error pull-left"></form:errors>
                        <form:select path="gender" cssClass="form-control">
                            <form:option value=""></form:option>
                            <form:option value="M"><fmt:message bundle="${res}" key="extinfo_gender_m" /></form:option>
                            <form:option value="F"><fmt:message bundle="${res}" key="extinfo_gender_f" /></form:option>
                        </form:select>
                    </div>
					<label for="birthdate" class="pull-left"><fmt:message bundle="${res}" key="extinfo_birthday" />:
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
						<label for="address" class="pull-left"><fmt:message bundle="${res}" key="extinfo_address" />: </label>
						<form:errors path="address" cssClass="error pull-left"></form:errors>
						<form:input path="address" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="city" class="pull-left"><fmt:message bundle="${res}" key="extinfo_city" />: </label>
						<form:errors path="city" cssClass="error pull-left"></form:errors>
						<form:input path="city" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="zip" class="pull-left"><fmt:message bundle="${res}" key="extinfo_zip" />: </label>
						<form:errors path="zip" cssClass="error pull-left"></form:errors>
						<form:input path="zip" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="country" class="pull-left"><fmt:message bundle="${res}" key="extinfo_country" />: </label>
						<form:errors path="country" cssClass="error pull-left"></form:errors>
						<form:select path="country" cssClass="form-control">
							<form:option value=""></form:option>
							<form:option value="IT"><fmt:message bundle="${res}" key="extinfo_country_italy" /></form:option>
							<form:option value="ES"><fmt:message bundle="${res}" key="extinfo_country_spain" /></form:option>
							<form:option value="RS"><fmt:message bundle="${res}" key="extinfo_country_serbia" /></form:option>
							<form:option value="FI"><fmt:message bundle="${res}" key="extinfo_country_finland" /></form:option>
						</form:select>
					</div>
					<div class="form-group">
						<label for="keywords" class="pull-left"><fmt:message bundle="${res}" key="extinfo_tags" />:</label>
						<form:errors path="keywords" cssClass="error pull-left"></form:errors>
						<form:input path="keywords" cssClass="form-control" />
					</div>
                    <div class="form-group">
                        <label for="role" class="pull-left"><fmt:message bundle="${res}" key="extinfo_role" />: </label>
                        <form:errors path="role" cssClass="error pull-left"></form:errors>
                        <form:select path="role" cssClass="form-control">
                            <form:option value=""></form:option>
                            <form:option value="Citizen"><fmt:message bundle="${res}" key="extinfo_role_citizen" /></form:option>
                            <form:option value="Academy"><fmt:message bundle="${res}" key="extinfo_role_academy" /></form:option>
                            <form:option value="Business"><fmt:message bundle="${res}" key="extinfo_role_business" /></form:option>
                        </form:select>
                    </div>
					<div class="form-group" style="text-align:left;">
                        <div><label><form:checkbox path="developer" disabled="true"/><fmt:message bundle="${res}" key="extinfo_developer" /></label></div>  
					</div>

					<label for="language" class="pull-left"><fmt:message bundle="${res}" key="extinfo_languages" />:</label>
							<form:errors path="language" cssClass="error pull-left"></form:errors>
							<br/>
						</div>
                        <div class="form-group" style="text-align:left;">
							<div><label><form:checkbox path="language" value="Italian" /><fmt:message bundle="${res}" key="extinfo_lang_it" /></label></div>  
                            <div><label><form:checkbox path="language" value="Spanish" /><fmt:message bundle="${res}" key="extinfo_lang_es" /></label></div>  
                            <div><label><form:checkbox path="language" value="Finnish" /><fmt:message bundle="${res}" key="extinfo_lang_fi" /></label></div>  
                            <div><label><form:checkbox path="language" value="Serbian" /><fmt:message bundle="${res}" key="extinfo_lang_rs" /></label></div>  
                            <div><label><form:checkbox path="language" value="SerbianLatin" /><fmt:message bundle="${res}" key="extinfo_lang_rsl" /></label></div>  
                            <div><label><form:checkbox path="language" value="English" /><fmt:message bundle="${res}" key="extinfo_lang_en" /></label></div>  
                        </div>
					<div class="button-row">
						<input type="submit" name="save" value="<fmt:message bundle="${res}" key="extinfo_save" />"
							class="btn btn-default" /> 
					</div>
				</form:form>
			</div>
		</div>
	</div>

	<script src="lib/jquery.js"></script>
	<script src="lib/bootstrap.min.js"></script>
	<script src="lib/moment.min.js"></script>
	<script src="lib/bootstrap-datetimepicker.min.js"></script>


	<script type="text/javascript">
            $(function () {
                $('#datetimepicker1').datetimepicker({
                	format: 'DD/MM/YYYY', viewMode: 'years'
                });
            });
        </script>
</body>
</html>