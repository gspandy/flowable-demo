
/**
 * 热门板块数据服务层
 * 
 * @author wengwh
 * @Date 2016-10-21
 * 
 */
angular.module('plumdo.services').service('StockHotPlateService',['$http', function($http) {
	return {
		getStockHotPlates : function(params) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getStockHotPlates(),
				params:params
			});
		},
		createStockHotPlate : function(data) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.createStockHotPlate(),
				data:data
			});
		},
		updateStockHotPlate : function(hotPlateId,data) {
			return $http({
				method: 'PUT',
				url:PLUMDO.URL.updateStockHotPlate(hotPlateId),
				data:data
			});
		},
		deleteStockHotPlate : function(hotPlateId) {
			return $http({
				method: 'DELETE',
				url:PLUMDO.URL.deleteStockHotPlate(hotPlateId)
			});
		},
		getStockHotPlate : function(hotPlateId) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getStockHotPlate(hotPlateId)
			});
		},
	};
}]);