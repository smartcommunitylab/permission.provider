angular.module('dev', [ 'ngResource' ]);

function AppController($scope, $resource) {
	$scope.app = null;
	$scope.clientId = 'none';
	$scope.clientView = 'none';
	
	var ClientAppBasic = $resource('dev/apps/:clientId', {}, {
		query : {
			method : 'GET',
			isArray : true
		}
	});
	
	var init = function() {
		$scope.apps = ClientAppBasic.query(function(apps){
			if (apps.length > 0) {
				$scope.app = apps[0];
				$scope.clientId = apps[0].clientId;
				$scope.switchClientView('overview');
			}
		});
	};
	init();

	$scope.activeClient = function(clientId) {
		var cls = clientId == $scope.clientId ? 'active' : '';
		return cls;
	};
	$scope.activeView = function(view) {
		return view == $scope.clientView ? 'active' : '';
	};
	
	$scope.switchClientView = function(view) {
		$scope.clientView = view;
	};
	$scope.switchClient = function(client) {
		for (var i = 0; i < $scope.apps.length; i++) {
			if ($scope.apps[i].clientId == client) {
				$scope.clientId = $scope.apps[i].clientId;
				$scope.app = $scope.apps[i];
				$scope.switchClientView('overview');
				return;
			}
		}
	};

	$scope.removeClient = function() {
		if (confirm('Are you sure you want to delete?')) {
			var newClient = new ClientAppBasic();
			newClient.$remove({clientId:$scope.clientId},function(app){
				init();
			});
	    }
	};

	$scope.newClient = function() {
		var n = prompt("Create new client app", "client name");
		if (n != null && n.trim().length > 0) {
			var newClient = new ClientAppBasic({name:n});
			newClient.$save(function(app){
				$scope.apps.push(app);
				$scope.switchClient(app.clientId);
			});
		}
	};

};
