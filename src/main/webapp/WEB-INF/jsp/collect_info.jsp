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
<title>Smart Community Authentication</title>
</head>
<body>
	<div class="container">
		<div class="row">
			<img class="logo" src="img/ls_logo.png" alt="SmartCommunity" />
		</div>
		<div class="row">
			<p>Please fill the form</p>
			<div role="form">
				<form:form method="POST" modelAttribute="info"
					action="/aac/collect-info">
					<div class="form-group">
						<label for="name" class="pull-left">Name:</label>
						<form:errors path="name" cssClass="error pull-left"></form:errors>
						<form:input path="name" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="surname" class="pull-left">Surname:</label>
						<form:errors path="surname" cssClass="error pull-left"></form:errors>
						<form:input path="surname" cssClass="form-control" />
					</div>
					<div class="form-group">
						<label for="email" class="pull-left">Email:</label>
						<form:errors path="email" cssClass="error pull-left"></form:errors>
						<form:input path="email" cssClass="form-control" />
					</div>
					<label for="birthdate" class="pull-left" >Birthdate:</label>
					<form:errors path="birthdate" cssClass="error pull-left"></form:errors>
					<div class="form-group" style="clear:both;">
						<div class="input-group" id="datetimepicker1">
							<input type='text' class="form-control" id="birthdate" name="birthdate" placeholder="30/01/1970"/> <span
								class="input-group-addon"> <span
								class="glyphicon glyphicon-calendar"></span>
							</span>
						</div>
					</div>
					<div class="form-group">
						<label for="keywords" class="pull-left">Keywords:</label>
						<form:errors path="keywords" cssClass="error pull-left"></form:errors>
						<form:input path="keywords" cssClass="form-control" />
					</div>
					<div>
						<input type="submit" value="Salva" class="btn btn-default" />
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
                	format: 'DD/MM/YYYY'
                });
            });
        </script>
</body>
</html>