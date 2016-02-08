<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

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
<title>WeLive users extended profile</title>

<style type="text/css">
.note {
	vertical-align: super;
}

.highlight {
	border: 1px solid #FF0900;
	padding: 2em;
	border-radius: 1em;
	margin: 1em;
	text-align: left;
}
</style>
</head>
<body>
	<div class="container">
		<div class="row">
			<img class="logo" src="img/welive-logo.png" alt="Welive" />
		</div>
		<div class="row">
			<h3>WeLive users extended profile</h3>
		</div>
		
		<div class="row">
			<div class="highlight">
				<p>
					The compilation of this form by the users of the WeLive framework
					is highly encourage, but is not compulsory and can be skipped with
					the button below. Besides, each single field in it is optional.
					Finally, please note that this is not a registration form.<br /> <span
						class="note"><strong>*</strong> </span>The information provided in the fields
					marked with an asterisk will be used to improve the recommendation
					that the WeLive framework will deliver to users about the services
					or other functionalities provided by the framework itself.
				</p>
			</div>
			<div role="form">
				<form:form method="POST" modelAttribute="info"
					action="/aac/collect-info">
          <div>
            <input type="submit" name="save" value="Save" class="btn btn-default" />
            <input type="submit" name="skip" value="Skip" class="btn btn-default" />
          </div>
          <div class="form-group">
            <label for="pilot" class="pull-left">Pilot<span
              class="note">*</span>:
            </label>
            <form:errors path="pilot" cssClass="error pull-left"></form:errors>
            <form:select path="pilot" cssClass="form-control">
              <form:option value=""></form:option>
              <form:option value="M">Trento</form:option>
              <form:option value="F">Bilbao</form:option>
              <form:option value="F">Novi Sad</form:option>
              <form:option value="F">Region on Uusimaa-Helsinki</form:option>
            </form:select>
          </div>
  				<div class="form-group">
						<label for="name" class="pull-left">Name<span
            class="note">*</span>:</label>
						<form:errors path="name" cssClass="error pull-left"></form:errors>
						<form:input path="name" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="surname" class="pull-left">Surname<span
            class="note">*</span>:</label>
						<form:errors path="surname" cssClass="error pull-left"></form:errors>
						<form:input path="surname" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="gender" class="pull-left">Gender<span
							class="note">*</span>:
						</label>
						<form:errors path="gender" cssClass="error pull-left"></form:errors>
						<form:select path="gender" cssClass="form-control">
							<form:option value=""></form:option>
							<form:option value="M">Male</form:option>
							<form:option value="F">Female</form:option>
						</form:select>
					</div>
					<div class="form-group">
						<label for="email" class="pull-left">Email<span
            class="note">*</span>:</label>
						<form:errors path="email" cssClass="error pull-left"></form:errors>
						<form:input path="email" cssClass="form-control" />
					</div>
					<label for="birthdate" class="pull-left">Birthdate<span
						class="note">*</span>:
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
						<label for="address" class="pull-left">Address: </label>
						<form:errors path="address" cssClass="error pull-left"></form:errors>
						<form:input path="address" cssClass="form-control" />
					</div>
          <div class="form-group">
            <label for="city" class="pull-left">City: </label>
            <form:errors path="city" cssClass="error pull-left"></form:errors>
            <form:input path="city" cssClass="form-control" />
          </div>
          <div class="form-group">
            <label for="zip" class="pull-left">Zip: </label>
            <form:errors path="zip" cssClass="error pull-left"></form:errors>
            <form:input path="zip" cssClass="form-control" />
          </div>
          <div class="form-group">
            <label for="country" class="pull-left">Country: </label>
            <form:errors path="country" cssClass="error pull-left"></form:errors>
            <form:select path="country" cssClass="form-control">
              <form:option value=""></form:option>
              <form:option value="IT">Italy</form:option>
              <form:option value="ES">Spain</form:option>
              <form:option value="RS">Serbia</form:option>
              <form:option value="FI">Finland</form:option>
            </form:select>
          </div>
					<div class="form-group">
						<label for="keywords" class="pull-left">Keywords:</label>
						<form:errors path="keywords" cssClass="error pull-left"></form:errors>
						<form:input path="keywords" cssClass="form-control" />
					</div>
          <div class="form-group">
            <label for="developer" class="pull-left">Developer:</label>
            <form:errors path="developer" cssClass="error pull-left"></form:errors>
            <form:checkbox  path="developer"/>
          </div>
          <div class="form-group">
            <label for="language" class="pull-left">Languages:</label>
            <form:errors path="language" cssClass="error pull-left"></form:errors>
            <form:checkbox  path="language" value="Italian"/>Italian
            <form:checkbox  path="language" value="Spanish"/>Spanish
            <form:checkbox  path="language" value="Finnish"/>Finnish
            <form:checkbox  path="language" value="Serbian"/>Serbian
            <form:checkbox  path="language" value="SerbianLatin"/>Serbian (Latin)
            <form:checkbox  path="language" value="English"/>English
          </div>
					<div>
						<input type="submit" name="save" value="Save" class="btn btn-default" />
						<input type="submit" name="skip" value="Skip" class="btn btn-default" />
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