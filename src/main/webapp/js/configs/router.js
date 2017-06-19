/**
 * 配置路由。
 * 注意这里采用的是ui-router这个路由，而不是ng原生的路由。
 * ng原生的路由不能支持嵌套视图，所以这里必须使用ui-router。
 */
angular.module('plumdo.configs').config(function($stateProvider, $urlRouterProvider, $ocLazyLoadProvider, IdleProvider, KeepaliveProvider) {
    IdleProvider.idle(5); // in seconds
    IdleProvider.timeout(120); // in seconds
	$urlRouterProvider.otherwise('/model');
	
	$stateProvider.state('model', {
		url : '/model',
		data: {pageTitle: ['流程模型'] },
		templateUrl: 'views/model/list.html',
		resolve: {
			loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
				return $ocLazyLoad.load([{files: ['js/services/model-service.js','js/controllers/model-controller.js']}]);
			}]
		}
	}).state('oa', {
		url : '/oa',
		data: {pageTitle: ['OA演示'] },
		templateUrl: 'views/oa/list.html',
		resolve: {
			loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
				return $ocLazyLoad.load([{files: ['js/services/oa-service.js','js/controllers/oa-controller.js']}]);
			}]
		}
	});
}).run(function($rootScope, $state) {
    $rootScope.$state = $state;
});