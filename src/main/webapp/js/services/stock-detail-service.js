/**************************用户列表****************************/
angular.module('plumdo.services').service('StockDetailService',['$http', function($http) { 
	return {
		getStockDetails : function(params) {
			return $http({
			 	method: 'GET', 
			 	url:PLUMDO.URL.getStockDetails(),
			 	params:params
			 });
		},
		createStockDetail : function(data){
			return $http({
			 	method: 'POST', 
			 	url:PLUMDO.URL.createStockDetail(),
			 	data:data
			 });
		},
		updateStockDetail : function(detailId,data){
			return $http({
			 	method: 'PUT', 
			 	url:PLUMDO.URL.updateStockDetail(detailId),
			 	data:data
			 });
		},
		deleteStockDetail : function(detailId){
			return $http({
			 	method: 'DELETE', 
			 	url:PLUMDO.URL.deleteStockDetail(detailId)
			 });
		},
		getStockDetail : function(detailId) {
			return $http({
			 	method: 'GET', 
			 	url:PLUMDO.URL.getStockDetail(detailId)
			 });
		},
		collectStockDetails : function(threadNum){
			return $http({
			 	method: 'PUT', 
			 	url:PLUMDO.URL.collectStockDetails(threadNum)
			 });
		},
	};    
}]);

    
