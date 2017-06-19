/**
 * 配置路由。
 * 注意这里采用的是ui-router这个路由，而不是ng原生的路由。
 * ng原生的路由不能支持嵌套视图，所以这里必须使用ui-router。
 */
angular.module('plumdo.configs').config(function($stateProvider, $urlRouterProvider, $ocLazyLoadProvider, IdleProvider, KeepaliveProvider) {
    IdleProvider.idle(5); // in seconds
    IdleProvider.timeout(120); // in seconds
	$urlRouterProvider.otherwise('/stock-info');
	
	$stateProvider.state('stock-info', {
		url : '/stock-info',
        data: {pageTitle: ['股票管理'] },
		templateUrl: 'views/stock-info/list.html',
		controller: 'StockInfoCtrl',
        resolve: {
            loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
                return $ocLazyLoad.load([{files: ['js/services/stock-info-service.js','js/controllers/stock-info-controller.js']}]);
            }]
        }
	}).state('stock-detail', {
		url : '/stock-detail',
        data: {pageTitle: ['股票详情'] },
		templateUrl: 'views/stock-detail/list.html',
		controller: 'StockDetailCtrl',
		params:{queryParams:{}},
        resolve: {
            loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
                return $ocLazyLoad.load([{files: ['js/services/stock-detail-service.js','js/controllers/stock-detail-controller.js']}]);
            }]
        }
	}).state('stock-report', {
		url : '/stock-report',
        data: { pageTitle: ['股票报表'] },
		abstract: true,   
		template: '<div ui-view></div>'
	}).state('stock-report.gold', {
		url : '/gold',
        data: {pageTitle: ['股票报表','黄金股详情'] },
		templateUrl: 'views/stock-report/gold-list.html',
		controller: 'StockReportCtrl',
        resolve: {
            loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
                return $ocLazyLoad.load([{files: ['js/services/stock-report-service.js','js/controllers/stock-report-controller.js']}]);
            }]
        }
	}).state('stock-report.weak', {
		url : '/weak',
        data: {pageTitle: ['股票报表','弱势股详情'] },
        template: '<div ui-view></div>',
		controller: 'StockReportCtrl',
        resolve: {
            loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
                return $ocLazyLoad.load([{files: ['js/services/stock-report-service.js','js/controllers/stock-report-controller.js']}]);
            }]
        }
	}).state('stock-report.weak.list', {
		url : '/list',
        data: {pageTitle: ['股票报表','弱势股详情'] },
		templateUrl: 'views/stock-report/weak-list.html',
	}).state('stock-report.weak.detail', {
		url : '/stock-detail',
        data: {pageTitle: ['股票详情'] },
		templateUrl: 'views/stock-detail/list.html',
		controller: 'StockDetailCtrl',
		params:{queryParams:{}},
        resolve: {
            loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
                return $ocLazyLoad.load([{files: ['js/services/stock-detail-service.js','js/controllers/stock-detail-controller.js']}]);
            }]
        }
	}).state('stock-hot-plate', {
		url : '/stock-hot-plate',
		data: {pageTitle: ['热门板块'] },
		templateUrl: 'views/stock-hot-plate/list.html',
		controller: 'StockHotPlateCtrl',
		resolve: {
			loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
				return $ocLazyLoad.load([{files: ['js/services/stock-hot-plate-service.js','js/controllers/stock-hot-plate-controller.js']}]);
			}]
		}
	}).state('stock-monster', {
		url : '/stock-monster',
		data: {pageTitle: ['妖股详情'] },
		templateUrl: 'views/stock-monster/list.html',
		controller: 'StockMonsterCtrl',
		resolve: {
			loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
				return $ocLazyLoad.load([{files: ['js/services/stock-info-service.js','js/services/stock-monster-service.js','js/controllers/stock-monster-controller.js']},{
                    name: 'ui.select',
                    files: ['libs/plugins/ui-select/select.min.js', 'libs/plugins/ui-select/select.min.css']
                }]);
			}]
		}
	}).state('lottery-detail', {
		url : '/lottery-detail',
		data: {pageTitle: ['六合彩详情'] },
		templateUrl: 'views/lottery-detail/list.html',
		resolve: {
			loadPlugin:["$ocLazyLoad", function ($ocLazyLoad) {
				return $ocLazyLoad.load([{files: ['js/services/lottery-detail-service.js','js/controllers/lottery-detail-controller.js']}]);
			}]
		}
	});
}).run(function($rootScope, $state) {
    $rootScope.$state = $state;
});