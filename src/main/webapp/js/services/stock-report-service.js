/**************************用户列表****************************/
angular.module('plumdo.services').service('StockReportService',['$http', function($http) { 
	return {
		getStockGolds : function(params) {
			return $http({
			 	method: 'GET', 
			 	url:PLUMDO.URL.getStockGolds(),
			 	params:params
			 });
		},
		getStockWeaks : function(params) {
			return $http({
			 	method: 'GET', 
			 	url:PLUMDO.URL.getStockWeaks(),
			 	params:params
			 });
		}
	};    
}]);