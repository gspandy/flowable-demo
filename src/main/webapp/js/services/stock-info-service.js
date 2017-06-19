/**************************用户列表****************************/
/**
 * 
 */
		
angular.module('plumdo.services').service('StockInfoService',['$http', function($http) { 
	return {
		getStockInfos : function(params) {
			return $http({
			 	method: 'GET', 
			 	url:PLUMDO.URL.getStockInfos(),
			 	params:params
			 });
		},
		createStockInfo : function(data){
			return $http({
			 	method: 'POST', 
			 	url:PLUMDO.URL.createStockInfo(),
			 	data:data
			 });
		},
		updateStockInfo : function(stockId,data){
			return $http({
			 	method: 'PUT', 
			 	url:PLUMDO.URL.updateStockInfo(stockId),
			 	data:data
			 });
		},
		deleteStockInfo : function(stockId){
			return $http({
			 	method: 'DELETE', 
			 	url:PLUMDO.URL.deleteStockInfo(stockId)
			 });
		},
		getStockInfo : function(stockId) {
			return $http({
			 	method: 'GET', 
			 	url:PLUMDO.URL.getStockInfo(stockId)
			 });
		},
	};    
}]);

    
