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

/*       @media (max-width: 980px) { 
         .navbar-text.pull-right { 
           float: none; 
           padding-left: 5px; 
           padding-right: 5px; 
         } 
       } */
    </style>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="js/html5shiv.js"></script>
    <![endif]-->
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
              <li ng-repeat="item in apps" class="{{activeClient(item.clientId)}}" ><a href ng-click="switchClient(item.clientId)">{{item.name}}</a></li>
            </ul>
          </div><!--/.well -->
        </div><!--/span-->
        <div class="span9 well">
            <div class="row">
                <div class="span7" ng-switch on="clientId">
                   <div ng-switch-when="none">&nbsp;</div>
                   <div ng-switch-default><strong>{{app.name}}</strong></div>
                </div>
                <div class="span2 text-right">
                    <div ng-switch on="clientId">
                        <div ng-switch-when="none">
                            <button class="btn btn-primary" type="button" ng-click="newClient()">New app</button>
                         </div>
                        <div ng-switch-default>
                            <button class="btn btn-primary" type="button" ng-click="newClient()">New app</button>
                            <button class="btn" type="button" ng-click="removeClient()">Delete</button>
                        </div>
                    </div>
                </div>
            </div>

			<div ng-switch on="clientId">
				<div ng-switch-when="none">
				    DEFAULT
				</div>
				<div ng-switch-default>
				    <ul class="nav nav-tabs">
                        <li class="{{activeView('overview')}}"><a href ng-click="switchClientView('overview')">Overview</a></li>
                        <li class="{{activeView('settings')}}"><a href ng-click="switchClientView('settings')">Settings</a></li>
                        <li class="{{activeView('permissions')}}"><a href ng-click="switchClientView('permissions')">Permissions</a></li>
                    </ul>
					<div ng-switch on="clientView">
						<div ng-switch-when="settings">SETTINGS</div>
						<div ng-switch-when="permissions">PERMISSIONS</div>
						<div ng-switch-when="overview">OVERVIEW</div>
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
