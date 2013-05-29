<!DOCTYPE html>
<html lang="en" ng-app="dev">
  <head>
    <meta charset="utf-8">
    <title>SmartCampus Developers</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
      .sidebar-nav {
        padding: 9px 0;
      }
    </style>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">

    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular-resource.min.js"></script>
    <script src="js/services.js"></script>
  </head>

  <body>

    <div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <div class="nav-collapse collapse">
            <p class="navbar-text pull-right">
              <a href="#" class="navbar-link">Username</a>
            </p>
            <ul class="nav">
              <li class="active"><a href="#">Accounts</a></li>
              <li><a href="#apps">Apps</a></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>

    <div class="container" ng-controller="AppController">
      <div class="row">
        <div class="span2">
          <div class="well sidebar-nav">
            <ul class="nav nav-list">
              <li class="nav-header">Client Apps</li>
              <li ng-repeat="item in apps" class="{{activeClient(item.clientId)}}" ><a href ng-click="switchClient(item.clientId)">{{item.name}}</a></li>
            </ul>
          </div><!--/.well -->
        </div><!--/span-->
        <div class="span9 well">
            <div class="alert alert-error" ng-show="error != ''">{{error}}</div>
            <div class="alert alert-success" ng-show="info != ''">{{info}}</div>
            <div class="row">
                <div class="span7" ng-switch on="clientId">
                   <div ng-switch-when="none">&nbsp;</div>
                   <div ng-switch-default><strong>{{app.name}}</strong></div>
                </div>
                <div class="span2 text-right">
                    <div ng-switch on="clientId">
                        <button class="btn btn-primary" type="button" ng-click="newClient()">New app</button>
                        <div ng-switch-when="none">&nbsp;</div>
                        <button ng-switch-default class="btn" type="button" ng-click="removeClient()">Delete</button>
                    </div>
                </div>
            </div>

			<div ng-switch on="clientId">
				<div ng-switch-when="none">
				    <h3>Welcome to SmartCampus developer console!</h3>
                    <p>Start here registering your applications.</p>
				</div>
				<div ng-switch-default>
				    <ul class="nav nav-tabs">
                        <li class="{{activeView('overview')}}"><a href ng-click="switchClientView('overview')">Overview</a></li>
                        <li class="{{activeView('settings')}}"><a href ng-click="switchClientView('settings')">Settings</a></li>
                        <li class="{{activeView('permissions')}}"><a href ng-click="switchClientView('permissions')">Permissions</a></li>
                    </ul>
					<div ng-switch on="clientView">
                        <div ng-switch-when="overview">
							<table class="table table-striped">
                                <tr><th>name</th><td>{{app.name}}</td></tr>
								<tr><th>clientId</th><td>{{app.clientId}}</td></tr>
                                <tr><th>clientSecret</th><td>{{app.clientSecret}}</td></tr>
                                <tr><th>redirect Web server URLs</th><td>{{app.redirectUris}}</td></tr>
                                <tr><th>Server-side access</th><td>{{app.serverSideAccess}}</td></tr>
                                <tr><th>Browser access</th><td>{{app.browserAccess}}</td></tr>
                                <tr><th>Native app access</th><td>{{app.nativeAppsAccess}}</td></tr>
							</table>
					    </div>
						<div ng-switch-when="settings">
						  <form ng-submit="saveSettings()">
                            <table class="table table-striped">
                                <tr><th>name</th><td><input type="text" class="input-large" placeholder="app_name" ng-model="app.name" required></td></tr>
                                <tr><th>clientId</th><td>{{app.clientId}} <a href="#">Reset</a></td></tr>
                                <tr><th>clientSecret</th><td>{{app.clientSecret}} <a href="#">Reset</a></td></tr>
                                <tr><th>redirect Web server URLs</th><td><input type="text" class="input-xxlarge" placeholder="specify your (comma-separated) web app addresses" ng-model="app.redirectUris"></td></tr>
                                <tr><th>Server-side access</th><td><input type="checkbox" ng-model="app.serverSideAccess"></td></tr>
                                <tr><th>Browser access</th><td><input type="checkbox" ng-model="app.browserAccess"></td></tr>
                                <tr><th>Native app access</th><td><input type="checkbox" ng-model="app.nativeAppsAccess"></td></tr>
                            </table>
                            <button type="submit" class="btn">Submit</button>
						  </form>
						</div>
						<div ng-switch-when="permissions">PERMISSIONS</div>
						<div ng-switch-default></div>
					</div>
				</div>
			</div>
		</div><!--/span-->
      </div><!--/row-->

      <hr>

      <footer>
        <p>&copy; SmartCampus 2013</p>
      </footer>

    </div>
  </body>
</html>
