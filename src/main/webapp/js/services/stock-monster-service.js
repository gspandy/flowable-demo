/**
 * 妖股详情数据服务层
 * 
 * @author wengwh
 * @Date 2016-10-25
 * 
 */
angular.module('plumdo.services').service('StockMonsterService',['$http', function($http) {
	return {
		getStockMonsters : function(params) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getStockMonsters(),
				params:params
			});
		},
		createStockMonster : function(data) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.createStockMonster(),
				data:data
			});
		},
		updateStockMonster : function(monsterId,data) {
			return $http({
				method: 'PUT',
				url:PLUMDO.URL.updateStockMonster(monsterId),
				data:data
			});
		},
		deleteStockMonster : function(monsterId) {
			return $http({
				method: 'DELETE',
				url:PLUMDO.URL.deleteStockMonster(monsterId)
			});
		},
		getStockMonster : function(monsterId) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getStockMonster(monsterId)
			});
		},
	};
}]);