angular.module('dev', [ 'ngResource' ]);

function MainController($scope) {
	$scope.currentView = 'apps';
	$scope.activeView = function(view) {
		return view == $scope.currentView ? 'active' : '';
	};
	$scope.signOut = function() {
	    window.document.location = "./logout";
	};
}

function ProfileController($scope) {
	
}

function AppController($scope, $resource) {
	$scope.app = null;
	$scope.clientId = 'none';
	$scope.clientView = 'overview';
	$scope.error = '';
	$scope.info = '';
	$scope.permissions = null;
	
	var ClientAppBasic = $resource('dev/apps/:clientId', {}, {
		query : { method : 'GET' },
		update : { method : 'PUT' },
		reset : {method : 'POST'}
	});

	var ClientAppPermissions = $resource('dev/permissions/:clientId', {}, {
		update : { method : 'PUT' },		
	});

	var ClientAppResourceParam = $resource('dev/resourceparams/:clientId/:resourceId/:value/', {}, {
		create : { method : 'POST' },		
	});
	
	var init = function() {
		ClientAppBasic.query(function(response){
			if (response.responseCode == 'OK') {
				$scope.error = '';
				var apps = response.data;
				$scope.apps = apps;
				if (apps.length > 0) {
					$scope.app = angular.copy(apps[0]);
					$scope.clientId = apps[0].clientId;
					$scope.switchClientView('overview');
				} else {
					$scope.clientId = 'none';
					$scope.app = null;
					$scope.switchClientView('overview');
				}
			} else {
				$scope.error = 'Failed to load apps: '+response.errorMessage;
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
	$scope.activePermView = function(view) {
		return view == $scope.permView ? 'active' : '';
	};
	
	$scope.switchClientView = function(view) {
		$scope.clientView = view;
		$scope.error = '';
		$scope.info = '';
	};

	$scope.switchPermView = function(view) {
		$scope.permView = view;
		$scope.error = '';
		$scope.info = '';
	};

	$scope.viewOverview = function() {
		$scope.switchClientView('overview');
	};

	$scope.viewSettings = function() {
		$scope.switchClientView('settings');
	};
	$scope.viewPermissions = function() {
		$scope.permissions = null;
		$scope.switchClientView('permissions');
		loadPermissions();
	};

	function loadPermissions() {
		var newClient = new ClientAppPermissions();
		newClient.$get({clientId:$scope.clientId}, function(response) {
			if (response.responseCode == 'OK') {
				$scope.error = '';
				$scope.permissions = response.data;
			} else {
				$scope.error = 'Failed to load app permissions: '+response.errorMessage;
			}	
		});
	}
	
	$scope.switchClient = function(client) {
		for (var i = 0; i < $scope.apps.length; i++) {
			if ($scope.apps[i].clientId == client) {
				$scope.clientId = $scope.apps[i].clientId;
				$scope.app = angular.copy($scope.apps[i]);
				$scope.switchClientView('overview');
				return;
			}
		}
	};

	$scope.removeClient = function() {
		if (confirm('Are you sure you want to delete?')) {
			var newClient = new ClientAppBasic();
			newClient.$remove({clientId:$scope.clientId},function(response){
				if (response.responseCode == 'OK') {
					$scope.error = '';
					init();
				} else {
					$scope.error = 'Failed to remove app: '+response.errorMessage;
				}	
			});
	    }
	};

	$scope.newClient = function() {
		var n = prompt("Create new client app", "client name");
		if (n != null && n.trim().length > 0) {
			var newClient = new ClientAppBasic({name:n});
			newClient.$save(function(response){
				if (response.responseCode == 'OK') {
					$scope.error = '';
					var app = response.data;
					$scope.apps.push(app);
					$scope.switchClient(app.clientId);
				} else {
					$scope.error = 'Failed to create new app: '+response.errorMessage;
				}
			});
		}
	};

	$scope.saveSettings = function() {
		var newClient = new ClientAppBasic($scope.app);
		newClient.$update({clientId:$scope.clientId}, function(response) {
			if (response.responseCode == 'OK') {
				$scope.error = '';
				$scope.info = 'App settings saved!';

				var app = response.data;
				$scope.app = angular.copy(app);
				for (var i = 0; i < $scope.apps.length; i++) {
					if ($scope.apps[i].clientId == app.clientId) {
						$scope.apps[i] = app;
						return;
					}
				}
			} else {
				$scope.error = 'Failed to save settings: '+response.errorMessage;
			}
		});
	};
	
	$scope.savePermissions = function() {
		var perm = new ClientAppPermissions($scope.permissions);
		perm.$update({clientId:$scope.clientId}, function(response) {
			if (response.responseCode == 'OK') {
				$scope.error = '';
				$scope.info = 'App permissions saved!';
				$scope.permissions = response.data;
			} else {
				$scope.error = 'Failed to save app permissions: '+response.errorMessage;
			}	
		});
	};
	$scope.saveResourceParam = function(resourceId,parentResource,serviceId,clientId) {
		var perm = new ClientAppResourceParam({resourceId:resourceId,parentResource:parentResource,serviceId:serviceId,clientId:clientId});
		var n = prompt("Create new resource", "value");
		if (n == null || n.trim().length==0) return;
		perm.value = n.trim();
		
		perm.$create(function(response) {
			if (response.responseCode == 'OK') {
				$scope.error = '';
				$scope.info = 'Resource added!';
				loadPermissions();
			} else {
				$scope.error = 'Failed to save own resource: '+response.errorMessage;
			}	
		});
	};

	$scope.filteredResources = function(resources, parentResource) {
		var arr = [];
		if (!resources) return arr;
		for (var i = 0; i < resources.length; i++) {
			if (resources[i].parentResource == parentResource) {
				arr.push(resources[i]);
			}
		}
		return arr;
	};
	
	$scope.removeResourceParam = function(r) {
		if (confirm('Are you sure you want to delete this resource and subresources?')) {
			var perm = new ClientAppResourceParam();
			perm.$delete({clientId:r.clientId,resourceId:r.resourceId,value:r.value},function(response){
				if (response.responseCode == 'OK') {
					$scope.error = '';
					$scope.info = 'Resource removed!';
					loadPermissions();
				} else {
					$scope.error = 'Failed to remove resource: '+response.errorMessage;
				}	
			});
	    }
	};

	$scope.permissionIcon = function(val) {
		switch (val){
		case 1: return 'icon-ok';
		case 2: return 'icon-remove';
		case 3: return 'icon-time';
		default: return null;
		}
		
	};
	
	$scope.statusIcon = function(val) {
		if (val) return 'icon-ok';
		else return 'icon-remove';
	};
	
	$scope.reset = function(client,param) {
		if (confirm('Are you sure you want to reset '+param+'?')) {
			var newClient = new ClientAppBasic($scope.app);
			newClient.$reset({clientId:$scope.clientId,reset:param}, function(response) {
				if (response.responseCode == 'OK') {
					$scope.error = '';
					$scope.info = param + ' successfully reset!';

					var app = response.data;
					for (var i = 0; i < $scope.apps.length; i++) {
						if ($scope.apps[i].clientId == client) {
							$scope.apps[i] = app;
							if ($scope.clientId == client) {
								$scope.clientId = app.clientId;
								$scope.app = angular.copy(app);
							}
							return;
						}
					}
				} else {
					$scope.error = 'Failed to reset '+param+': '+response.errorMessage;
				}
			});
		}
	};
};
