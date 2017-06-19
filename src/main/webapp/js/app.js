(function (angular) {
	'use strict';
	
    angular.module('plumdo', [
        'ui.router',                    // Routing
        'oc.lazyLoad',                  // ocLazyLoad
        'ui.bootstrap',                 // Ui Bootstrap
        'cgNotify',
        'ngIdle',                       // Idle timer
        'ngSanitize',
        'plumdo.factorys',
        'plumdo.directives',
        'plumdo.configs',
        'plumdo.filters',
        'plumdo.controllers',
        'plumdo.services'
    ]).run(function($rootScope, $state,$uibModal,notify) {
    	
    	$rootScope.progressNum = 0;
    	
    	$rootScope.showProgress = function (msg) {
    		$rootScope.progressNum++;
    		if(msg){
    			$rootScope.showSuccessMsg(msg);
        	}
        };
        
        $rootScope.hideProgressBySucess = function(msg){
        	$rootScope.hideProgress(msg,'notify-success');
        };
        
        $rootScope.hideProgressByError = function(msg){
        	$rootScope.hideProgress(msg,'notify-error');
        };
        
        $rootScope.hideProgress = function (msg,classes) {
        	$rootScope.progressNum--;
        	if(msg){
        		if(classes && classes!=null){
                	$rootScope.showMsg(msg,2000,classes);
        		}else{
                	$rootScope.showErrorMsg(msg);
        		}
        	}
        };
        
    	$rootScope.showSuccessMsg = function (msg) {
        	$rootScope.showMsg(msg,2000,'notify-success');
        };
        
        $rootScope.showErrorMsg = function (msg) {
        	$rootScope.showMsg(msg,5000,'notify-error');
        };
        
        $rootScope.showMsg = function (msg,duration,classes) {
    		notify({ message: msg, duration:duration,position:'right',classes: classes});
        };
        
        $rootScope.confirmModal = function(args) {
			$uibModal.open({
	            templateUrl: 'views/common/confirm-modal.html',
                controller:function($scope,$uibModalInstance){
                	$scope.modalTitle = angular.copy(args.title);
                	$scope.cancel = function(){
        		        $uibModalInstance.dismiss('cancel');
                    	args.confirm(false);
                    };
                    $scope.ok = function(){
                    	$uibModalInstance.close();
                    	args.confirm(true);
                    };
                }
	        });
		};
        
    });

    angular.module('plumdo.configs', []);
	angular.module('plumdo.services', []);
    angular.module('plumdo.controllers', []);
    angular.module('plumdo.directives', []);
	angular.module('plumdo.filters', []);
	angular.module('plumdo.factorys', []);
	angular.module('plumdo.constants', []);

})(angular);

