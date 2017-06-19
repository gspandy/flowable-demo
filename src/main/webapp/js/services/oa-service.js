/**
 * OA数据服务层
 * 
 * @author wengwh
 * @Date 2017-06-19
 * 
 */
angular.module('plumdo.services').service('OaService',['$http', function($http) {
	return {
		getOas : function(params) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getOas(),
				params:params
			});
		},
		startOa : function(data) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.startOa(),
				data:data
			});
		},
		getOa : function(oaId) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getOa(oaId)
			});
		},
		completeOa : function(oaId) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.completeOa(oaId)
			});
		},
		returnOa : function(oaId) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.returnOa(oaId)
			});
		}
	};
}]);