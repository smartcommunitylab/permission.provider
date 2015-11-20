<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="../css/style.css" rel="stylesheet" type="text/css">
<title>Smart Community Authentication</title>
</head>
<body>
	<img class="logo" src="../img/ls_logo.png" alt="SmartCommunity" />
	<div class="clear"></div>
	<p>
	Please fill the form
	<form:form method="POST" modelAttribute="info" action="/aac/extra-info">
		<form:input path="name"/>
		<form:input path="surname"/>
		<input type="submit" value="Salva"/>
	</form:form>
	</p>
	</div>
</body>
</html>